package edu.skku.inho.colorize;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Process;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import edu.skku.inho.colorize.IconGroupingModule.GroupColor;
import edu.skku.inho.colorize.IconGroupingModule.IconColorGrouper;
import edu.skku.inho.colorize.SettingPage.SplashActivity;

public class UpdateService extends Service {
	private static final String TAG = "UpdateService";

	private int mGroupingMode = -1;

	private BroadcastReceiver mScreenStateReceiver;

	private Handler mUpdateHandler;

	private Runnable mIconColorGroupingRunnable = new Runnable() {
		@Override
		public void run() {
			LockScreenDataProvider lockScreenDataProvider = LockScreenDataProvider.getInstance(UpdateService.this);

			lockScreenDataProvider.setIsColorDataReady(false);

			IconColorGrouper iconColorGrouper;
			if (mGroupingMode == Constants.GROUPING_WITH_FIXED_COLOR_MODE) {
				Log.i(TAG, "[Grouping Mode] fixed color");
				iconColorGrouper = IconColorGrouper.groupFrom(readAppInfoFromDevice(), UpdateService.this).generateWithFixedColor();
			} else {
				Log.i(TAG, "[Grouping Mode] variable color");
				iconColorGrouper = IconColorGrouper.groupFrom(readAppInfoFromDevice(), UpdateService.this).generateWithVariableColor();
			}

			lockScreenDataProvider.setGroupColorList((ArrayList<GroupColor>) iconColorGrouper.getGroupColorList());
			lockScreenDataProvider.setApplicationList((ArrayList<ApplicationInfoBundle>) iconColorGrouper.getApplicationList());


			// notify SettingActivity that color computing is finished
			sendColorDataReadyStateMessage();

			lockScreenDataProvider.setIsColorDataReady(true);

			// check all applications' installation state every N seconds
			mUpdateHandler.postDelayed(mIconColorGroupingRunnable, computeApplicationListChangeCheckingPeriod());
		}
	};


	public UpdateService() {
	}

	@Override
	public void onCreate() {
		super.onCreate();
		configureScreenStateReceiver();
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		if (intent != null) {
			if (intent.getAction() == null) {
				if (mScreenStateReceiver == null) {
					configureScreenStateReceiver();
				}
			}
		}

		configureGroupingMode(intent);

		// start thread for color analyzing
		HandlerThread updateHandlerThread = new HandlerThread("UpdateColorData", Process.THREAD_PRIORITY_BACKGROUND);
		updateHandlerThread.start();

		startForeground(1, getNotification());
		addHandlerToHandlerThread(updateHandlerThread);

		LockScreenDataProvider.getInstance(this).setIsLockScreenRunning(true);

		return START_REDELIVER_INTENT;
	}

	@Override
	public void onDestroy() {
		Log.d(TAG, "Service on destroy...");
		// remove appropriate runnable of each mode from handler
		if (mGroupingMode == Constants.GROUPING_WITH_FIXED_COLOR_MODE) {
			Log.d(TAG, "fixed_color_mode_terminated");
		} else {
			Log.d(TAG, "variable_color_mode_terminated");
		}

		mUpdateHandler.removeCallbacks(mIconColorGroupingRunnable);

		sendFinishMessage();

		// save stopped lock screen running state into shared preferences
		LockScreenDataProvider.getInstance(this).setIsLockScreenRunning(false);

		unregisterReceiver(mScreenStateReceiver);

		super.onDestroy();
	}

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	private void sendFinishMessage() {
		Log.d(TAG, "sending lock screen termination message to LockScreenActivity...");
		Intent intent = new Intent(Keys.UPDATE_SERVICE_BROADCAST);
		intent.putExtra(Keys.UPDATE_SERVICE_MESSAGE, Constants.UPDATE_SERVICE_DESTROYED);
		LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
	}

	private void configureGroupingMode(Intent intent) {
		// branch made to guarantee available group mode
		if (intent != null) {
			mGroupingMode = intent.getIntExtra(Keys.GROUPING_MODE, Constants.GROUPING_WITH_FIXED_COLOR_MODE);
		} else {
			mGroupingMode = LockScreenDataProvider.getInstance(this).getGroupingMode();
		}
	}

	private Notification getNotification() {
		String contentTitle = getResources().getString(R.string.colorize_running);

		if (mGroupingMode == Constants.GROUPING_WITH_FIXED_COLOR_MODE) {
			contentTitle += getResources().getString(R.string.grouping_with_fixed_color_mode_running);
		} else {
			contentTitle += getResources().getString(R.string.grouping_with_variable_color_mode_running);
		}

		Notification.Builder builder = new Notification.Builder(this).setSmallIcon(R.mipmap.ic_launcher).setContentTitle(contentTitle)
				.setContentText(getResources().getString(R.string.click_to_configure_colorize));

		Intent settingActivityIntent = new Intent(this, SplashActivity.class);
		PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, settingActivityIntent, 0);
		builder.setContentIntent(pendingIntent);

		return builder.build();
	}

	private void addHandlerToHandlerThread(HandlerThread handlerThread) {
		mUpdateHandler = new Handler(handlerThread.getLooper());
		mUpdateHandler.post(mIconColorGroupingRunnable);
	}

	private void configureScreenStateReceiver() {
		mScreenStateReceiver = new ScreenStateReceiver();
		registerReceiver(mScreenStateReceiver, new IntentFilter(Intent.ACTION_SCREEN_OFF));
	}

	private int computeApplicationListChangeCheckingPeriod() {
		int applicationListChangeCheckingPeriod = getResources()
				.getIntArray(R.array.application_list_change_checking_period_seconds)[LockScreenDataProvider.getInstance(this)
				.getApplicationListChangeCheckingPeriodChoiceIndex()] * 1000;
		Log.d(TAG, "[Change Checking Period]" + " " + applicationListChangeCheckingPeriod);
		return applicationListChangeCheckingPeriod;
	}

	private void sendColorDataReadyStateMessage() {
		Log.d(TAG, "sending color data ready state message...");
		Intent intent = new Intent(Keys.UPDATE_SERVICE_BROADCAST);
		intent.putExtra(Keys.UPDATE_SERVICE_MESSAGE, Constants.COLOR_DATA_READY);
		LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
	}

	protected List<ResolveInfo> readAppInfoFromDevice() {
		Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
		mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);
		return getPackageManager().queryIntentActivities(mainIntent, PackageManager.PERMISSION_GRANTED);
	}
}
