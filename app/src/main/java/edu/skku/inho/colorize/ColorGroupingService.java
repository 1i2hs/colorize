package edu.skku.inho.colorize;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Binder;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Process;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import edu.skku.inho.colorize.IconGroupingModule.GroupColor;
import edu.skku.inho.colorize.IconGroupingModule.IconColorGrouper;
import edu.skku.inho.colorize.SettingPage.SplashActivity;

/**
 * Created by In-Ho Han on 2/11/16.
 */
public class ColorGroupingService extends Service {
	private static final String TAG = "ColorGroupingService";

	private final IBinder mBinder = new ColorGroupingLocalBinder();

	private ScreenStateReceiver mScreenStateReceiver;

	private BroadcastReceiver mPackageInstallationStateReceiver;

	private BroadcastReceiver mServiceTerminationReceiver;

	private Handler mUpdateHandler;

	private Runnable mIconColorGroupingRunnable = new Runnable() {
		@Override
		public void run() {
			LockScreenDataManager lockScreenDataManager = LockScreenDataManager.getInstance(ColorGroupingService.this);

			lockScreenDataManager.setIsColorDataReady(false);

			IconColorGrouper iconColorGrouper;
			if (lockScreenDataManager.getGroupingMode() == Constants.GROUPING_WITH_FIXED_COLOR_MODE) {
				Log.i(TAG, "[Grouping Mode] fixed color");
				iconColorGrouper = IconColorGrouper.groupFrom(readAppInfoFromDevice(), ColorGroupingService.this).generateWithFixedGroupColor();
			} else {
				Log.i(TAG, "[Grouping Mode] variable color");
				iconColorGrouper = IconColorGrouper.groupFrom(readAppInfoFromDevice(), ColorGroupingService.this).generateWithCalculatedGroupColor();
			}

			lockScreenDataManager.setGroupColorList((ArrayList<GroupColor>) iconColorGrouper.getGroupColorList());
			lockScreenDataManager.setApplicationList((ArrayList<ApplicationInfoBundle>) iconColorGrouper.getApplicationList());

			// notify SettingActivity that color computing is finished
			sendColorDataReadyStateMessage();

			lockScreenDataManager.setIsColorDataReady(true);
		}
	};

	public ColorGroupingService() {
	}

	@Override
	public void onCreate() {
		super.onCreate();
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		if (intent != null) {
			if (intent.getAction() == null) {
				if (mScreenStateReceiver == null) {
					configureScreenStateReceiver();
				}
				if (mPackageInstallationStateReceiver == null) {
					configurePackageInstallationStateReceiver();
				}
				if (mServiceTerminationReceiver == null) {
					configureServiceTerminationReceiver();
				}
			}
		}

		// start thread for color analyzing
		HandlerThread updateHandlerThread = new HandlerThread("UpdateColorData", Process.THREAD_PRIORITY_BACKGROUND);
		updateHandlerThread.start();

		startForeground(1, getNotification());
		addHandlerToHandlerThread(updateHandlerThread);

		LockScreenDataManager.getInstance(this).setIsLockScreenRunning(true);

		return START_REDELIVER_INTENT;
	}

	@Override
	public void onDestroy() {
		Log.d(TAG, "Service on destroy...");
		// remove appropriate runnable of each mode from handler
		if (LockScreenDataManager.getInstance(this).getGroupingMode() == Constants.GROUPING_WITH_FIXED_COLOR_MODE) {
			Log.d(TAG, "fixed_color_mode_terminated");
		} else {
			Log.d(TAG, "variable_color_mode_terminated");
		}

		mUpdateHandler.removeCallbacks(mIconColorGroupingRunnable);

		sendFinishMessage();

		unregisterReceiver(mScreenStateReceiver);
		unregisterReceiver(mPackageInstallationStateReceiver);
		unregisterReceiver(mServiceTerminationReceiver);

		super.onDestroy();
	}

	@Override
	public IBinder onBind(Intent intent) {
		Log.i(TAG, "bind with other component...");
		return mBinder;
	}

	@Override
	public boolean onUnbind(Intent intent) {
		Log.i(TAG, "unbind with other component...");
		return super.onUnbind(intent);
	}

	private void sendFinishMessage() {
		Log.d(TAG, "sending lock screen termination message to LockScreenActivity...");
		Intent intent = new Intent(Keys.COLOR_GROUPING_SERVICE_BROADCAST);
		intent.putExtra(Keys.COLOR_GROUPING_SERVICE_MESSAGE, Constants.COLOR_GROUPING_SERVICE_DESTROYED);
		LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
	}

	private void configureScreenStateReceiver() {
		mScreenStateReceiver = new ScreenStateReceiver();
		registerReceiver(mScreenStateReceiver, new IntentFilter(Intent.ACTION_SCREEN_OFF));
	}

	private void configurePackageInstallationStateReceiver() {
		mPackageInstallationStateReceiver = new BroadcastReceiver() {
			@Override
			public void onReceive(Context context, Intent intent) {
				// if there are any package installation changes happening, re-compute each application's icon's color
				computeGroupColors();
			}
		};
		IntentFilter packageInstallationStateIntentFilter = new IntentFilter();
		packageInstallationStateIntentFilter.addAction(Intent.ACTION_PACKAGE_ADDED);
		packageInstallationStateIntentFilter.addAction(Intent.ACTION_PACKAGE_CHANGED);
		packageInstallationStateIntentFilter.addAction(Intent.ACTION_PACKAGE_REMOVED);
		packageInstallationStateIntentFilter.addAction(Intent.ACTION_PACKAGE_REPLACED);
		packageInstallationStateIntentFilter.addDataScheme("package");
		registerReceiver(mPackageInstallationStateReceiver, packageInstallationStateIntentFilter);
	}

	private void configureServiceTerminationReceiver() {
		mServiceTerminationReceiver = new BroadcastReceiver() {
			@Override
			public void onReceive(Context context, Intent intent) {
				LockScreenDataManager.getInstance(context).setIsLockScreenRunning(false);
				stopSelf();
			}
		};
		registerReceiver(mServiceTerminationReceiver, new IntentFilter(Keys.NOTIFICATION_BROADCAST));
	}

	private Notification getNotification() {
		String contentTitle = getResources().getString(R.string.colorize_running);

		Intent settingActivityIntent = new Intent(this, SplashActivity.class);
		PendingIntent settingActivityPendingIntent = PendingIntent.getActivity(this, 0, settingActivityIntent, 0);

		Intent serviceTerminationReceiverIntent = new Intent(Keys.NOTIFICATION_BROADCAST);
		PendingIntent serviceTerminationReceiverPendingIntent = PendingIntent.getBroadcast(this, 0, serviceTerminationReceiverIntent, 0);

		NotificationCompat.Builder builder = new NotificationCompat.Builder(this).setSmallIcon(R.mipmap.ic_notification)
				.setColor(getResources().getColor(R.color.colorPrimary)).setContentTitle(contentTitle)
				.setContentText(getResources().getString(R.string.click_to_configure_colorize))
				.addAction(R.drawable.ic_stop_black_24dp, getResources().getString(R.string.stop), serviceTerminationReceiverPendingIntent);

		builder.setContentIntent(settingActivityPendingIntent);

		return builder.build();
	}

	private void addHandlerToHandlerThread(HandlerThread handlerThread) {
		mUpdateHandler = new Handler(handlerThread.getLooper());
		mUpdateHandler.post(mIconColorGroupingRunnable);
	}

	public void computeGroupColors() {
		mUpdateHandler.removeCallbacks(mIconColorGroupingRunnable);
		mUpdateHandler.post(mIconColorGroupingRunnable);
	}

	private void sendColorDataReadyStateMessage() {
		Log.i(TAG, "sending color data ready state message...");
		Intent intent = new Intent(Keys.COLOR_GROUPING_SERVICE_BROADCAST);
		intent.putExtra(Keys.COLOR_GROUPING_SERVICE_MESSAGE, Constants.COLOR_DATA_READY);
		LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
	}

	protected List<ResolveInfo> readAppInfoFromDevice() {
		Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
		mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);
		return getPackageManager().queryIntentActivities(mainIntent, PackageManager.PERMISSION_GRANTED);
	}

	public class ColorGroupingLocalBinder extends Binder {
		public ColorGroupingService getService() {
			return ColorGroupingService.this;
		}
	}
}
