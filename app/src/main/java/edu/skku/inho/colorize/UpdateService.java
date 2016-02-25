package edu.skku.inho.colorize;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Process;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.graphics.Palette;
import android.util.Log;
import android.util.TypedValue;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import edu.skku.inho.colorize.IconSortingModule.Color;
import edu.skku.inho.colorize.IconSortingModule.DrawableToBitmapConverter;
import edu.skku.inho.colorize.IconSortingModule.GroupColor;
import edu.skku.inho.colorize.IconSortingModule.HighlyPopulatedColorExtractor;
import edu.skku.inho.colorize.IconSortingModule.KMeans;
import edu.skku.inho.colorize.IconSortingModule.RGBToCIELabConverter;
import edu.skku.inho.colorize.SettingPage.SettingActivity;

public class UpdateService extends Service {
	private static final String TAG = "UpdateService";

	private int[] mFixedGroupColorIds = {R.color.fixed_color_one, R.color.fixed_color_two, R.color.fixed_color_three, R.color.fixed_color_four, R.color.fixed_color_five, R.color.fixed_color_six, R.color.fixed_color_seven, R.color.fixed_color_eight};

	private boolean mIsGroupingWithFixedColorModeInitialized = true;

	private int mGroupingMode = -1;

	private BroadcastReceiver mScreenStateReceiver;

	private Handler mUpdateHandler;

	private Runnable mGroupingWithFixedColorModeRunnable = new Runnable() {
		@Override
		public void run() {
			Log.i(TAG, "fixed color grouping mode is enabled...");

			matchExtractedColorsToGroupColors(loadFixedGroupColors(), makeExtractedColorPointList(readAppInfoFromDevice()));

			// notify SettingActivity that color computing is finished
			sendColorDataReadyStateMessage();

			//mSharedPreferences.edit().putBoolean(Keys.IS_COLOR_DATA_READY, true).apply();
			LockScreenDataProvider.getInstance(UpdateService.this).setIsColorDataReady(true);

			// check all applications' installation state every N seconds
			mUpdateHandler.postDelayed(mGroupingWithFixedColorModeRunnable, computeApplicationListChangeCheckingPeriod());
		}
	};
	private Runnable mGroupingWithVariableColorModeRunnable = new Runnable() {
		@Override
		public void run() {
			Log.i(TAG, "variable color grouping mode is enabled...");
			//mSharedPreferences.edit().putBoolean(Keys.IS_COLOR_DATA_READY, false).apply();
			LockScreenDataProvider.getInstance(UpdateService.this).setIsColorDataReady(false);

			// begin computing 8 colors;
			LockScreenDataProvider.getInstance(UpdateService.this).setGroupColorList(makeGroupColorList());

			// notify SettingActivity that color computing is finished
			sendColorDataReadyStateMessage();

			//mSharedPreferences.edit().putBoolean(Keys.IS_COLOR_DATA_READY, true).apply();
			LockScreenDataProvider.getInstance(UpdateService.this).setIsColorDataReady(true);

			// check all applications' installation state every N seconds
			mUpdateHandler.postDelayed(mGroupingWithVariableColorModeRunnable, computeApplicationListChangeCheckingPeriod());
		}
	};

	public UpdateService() {
	}

	private ArrayList<GroupColor> loadFixedGroupColors() {
		if (mIsGroupingWithFixedColorModeInitialized) {
			ArrayList<GroupColor> groupColorList = new ArrayList<>();
			for (int i = 0; i < mFixedGroupColorIds.length; i++) {
				double[] CIELab;
				if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
					CIELab = RGBToCIELabConverter.convertRGBToLab(getResources().getColor(mFixedGroupColorIds[i]));
				} else {
					CIELab = RGBToCIELabConverter.convertRGBToLab(getColor(mFixedGroupColorIds[i]));

				}
				groupColorList.add(new GroupColor(CIELab));
			}

			return groupColorList;
		} else {
			return null;
		}
	}

	private void matchExtractedColorsToGroupColors(ArrayList<GroupColor> groupColorList, ArrayList<Color> extractedColorList) {
		for (Color extractedColor : extractedColorList) {
			double minimumEuclideanDistance = Double.MAX_VALUE;
			int minimumEuclideanDistanceIndex = -1;

			for (int i = 0; i < groupColorList.size(); i++) {
				double temp = computeEuclideanDistanceBetweenTwoColors(groupColorList.get(i), extractedColor);
				if (temp < minimumEuclideanDistance) {
					minimumEuclideanDistance = temp;
					minimumEuclideanDistanceIndex = i;
				}
				if (i == groupColorList.size() - 1) {
					ArrayList<ApplicationInfoBundle> applicationList = groupColorList.get(minimumEuclideanDistanceIndex).getApplicationList();
					boolean isApplicationAlreadyInsideList = false;
					for (int j = 0; j < applicationList.size(); j++) {
						if (extractedColor.getApplicationInfoBundle().getApplicationName().equals(applicationList.get(j).getApplicationName())) {
							isApplicationAlreadyInsideList = true;
							break;
						}
					}
					if (!isApplicationAlreadyInsideList) {
						groupColorList.get(minimumEuclideanDistanceIndex).addApplicationInfoBundle(extractedColor.getApplicationInfoBundle());
					}
				}
			}
		}
		for (GroupColor groupColor : groupColorList) {
			groupColor.setApplicationList(sortApplicationInAlphabeticalOrder(groupColor.getApplicationList()));
		}

		LockScreenDataProvider.getInstance(this).setGroupColorList(groupColorList);
	}

	private double computeEuclideanDistanceBetweenTwoColors(GroupColor groupColor, Color extractedColor) {
		return Math.sqrt(Math.pow((groupColor.getCIELabColor()[0] - extractedColor.getL()), 2) +
				Math.pow((groupColor.getCIELabColor()[1] - extractedColor.getA()), 2) +
				Math.pow((groupColor.getCIELabColor()[2] - extractedColor.getB()), 2));
	}

	/**
	 * must be merged with equal method in splash activity
	 *
	 * @param applicationList
	 * @return
	 */
	private ArrayList<ApplicationInfoBundle> sortApplicationInAlphabeticalOrder(ArrayList<ApplicationInfoBundle> applicationList) {
		Collections.sort(applicationList, new Comparator<ApplicationInfoBundle>() {
			@Override
			public int compare(ApplicationInfoBundle o1, ApplicationInfoBundle o2) {
				return o1.getApplicationName().compareToIgnoreCase(o2.getApplicationName());
			}
		});
		return applicationList;
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
		//configureApplicationListChangeCheckingPeriod();

		// start thread for color analyzing
		HandlerThread updateHandlerThread = new HandlerThread("UpdateColorData", Process.THREAD_PRIORITY_BACKGROUND);
		updateHandlerThread.start();

		startForeground(1, getNotification());
		addHandlerToHandlerThread(updateHandlerThread);

		//mSharedPreferences.edit().putBoolean(Keys.IS_LOCK_SCREEN_RUNNING, true).apply();
		LockScreenDataProvider.getInstance(this).setIsLockScreenRunning(true);

		return START_REDELIVER_INTENT;
	}

	@Override
	public void onDestroy() {
		Log.d(TAG, "Service on destroy...");
		// remove appropriate runnable of each mode from handler
		if (mGroupingMode == Constants.GROUPING_WITH_FIXED_COLOR_MODE) {
			Log.d(TAG, "fixed_color_mode_terminated");
			mUpdateHandler.removeCallbacks(mGroupingWithFixedColorModeRunnable);
		} else {
			Log.d(TAG, "variable_color_mode_terminated");
			mUpdateHandler.removeCallbacks(mGroupingWithVariableColorModeRunnable);
		}

		sendFinishMessage();

		// save stopped lock screen running state into shared preferences
		//mSharedPreferences.edit().putBoolean(Keys.IS_LOCK_SCREEN_RUNNING, false).apply();
		LockScreenDataProvider.getInstance(this).setIsLockScreenRunning(false);

		unregisterReceiver(mScreenStateReceiver);

		super.onDestroy();
	}

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	private void sendFinishMessage() {
		Log.d(TAG, "sending lock screen termination message to MainActivity...");
		Intent intent = new Intent(Keys.UPDATE_SERVICE_BROADCAST);
		intent.putExtra(Keys.UPDATE_SERVICE_MESSAGE, Constants.UPDATE_SERVICE_DESTROYED);
		LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
	}

	private void configureGroupingMode(Intent intent) {
		// branch made to guarantee available group mode
		if (intent != null) {
			mGroupingMode = intent.getIntExtra(Keys.GROUPING_MODE, Constants.GROUPING_WITH_FIXED_COLOR_MODE);
		} else {
			//mGroupingMode = mSharedPreferences.getInt(Keys.GROUPING_MODE, Constants.GROUPING_WITH_FIXED_COLOR_MODE);
			mGroupingMode = LockScreenDataProvider.getInstance(this).getGroupingMode();
		}
		Log.i(TAG, "Grouping mode : " + mGroupingMode);
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

		Intent settingActivityIntent = new Intent(this, SettingActivity.class);
		PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, settingActivityIntent, 0);
		builder.setContentIntent(pendingIntent);

		return builder.build();
	}

	private void addHandlerToHandlerThread(HandlerThread handlerThread) {
		mUpdateHandler = new Handler(handlerThread.getLooper());
		if (mGroupingMode == Constants.GROUPING_WITH_FIXED_COLOR_MODE) {
			mUpdateHandler.post(mGroupingWithFixedColorModeRunnable);
		} else {
			mUpdateHandler.post(mGroupingWithVariableColorModeRunnable);
		}
	}

	private void configureScreenStateReceiver() {
		mScreenStateReceiver = new ScreenStateReceiver();
		registerReceiver(mScreenStateReceiver, new IntentFilter(Intent.ACTION_SCREEN_OFF));
	}

	private int computeApplicationListChangeCheckingPeriod() {
		return getResources().getIntArray(R.array.application_list_change_checking_period_seconds)[LockScreenDataProvider.getInstance(this)
				.getApplicationListChangeCheckingPeriodChoiceIndex()] * 1000;
	}

	private void sendColorDataReadyStateMessage() {
		Log.d(TAG, "sending color data ready state message...");
		Intent intent = new Intent(Keys.UPDATE_SERVICE_BROADCAST);
		intent.putExtra(Keys.UPDATE_SERVICE_MESSAGE, Constants.COLOR_DATA_READY);
		LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
	}

	private ArrayList<GroupColor> makeGroupColorList() {
		ArrayList<GroupColor> groupColorList = new ArrayList<>();
		for (KMeans.Group group : groupExtractedColorPointsIntoEightColorPoints(makeExtractedColorPointList(readAppInfoFromDevice()))) {
			groupColorList.add(mapGroupToGroupColor(group));
		}
		return groupColorList;
	}

	private List<KMeans.Group> groupExtractedColorPointsIntoEightColorPoints(ArrayList<Color> extractedColorList) {
		Log.i(TAG, ">>> Starting color grouping...");
		//KMeans kMeans = new KMeans(LockScreenDataProvider.getInstance().getExtractedColorPointList());
		KMeans kMeans = new KMeans(extractedColorList);
		kMeans.init(this);
		kMeans.calculate();
		Log.i(TAG, ">>> Grouping colors completed");
		return kMeans.getGroups();
	}

	private ArrayList<Color> makeExtractedColorPointList(List<ResolveInfo> applicationResolveInfoList) {
		ArrayList<ApplicationInfoBundle> applicationList = new ArrayList<>();
		ArrayList<Color> extractedColorList = new ArrayList<>();
		int drawableSize = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 56, getResources().getDisplayMetrics());

		for (ResolveInfo resolveInfo : applicationResolveInfoList) {
			ApplicationInfo temp = resolveInfo.activityInfo.applicationInfo;
			ApplicationInfoBundle applicationInfoBundle = new ApplicationInfoBundle();

			Drawable tempDrawable = temp.loadIcon(getPackageManager());
			applicationInfoBundle.setApplicationIcon(tempDrawable);
			applicationInfoBundle.setApplicationPackageName(temp.packageName);
			applicationInfoBundle.setApplicationName(temp.loadLabel(getPackageManager()).toString());
			//applicationInfoBundle.setIntentForPackage(getPackageManager().getLaunchIntentForPackage(temp.packageName));
			extractSevenColors(Palette.from(DrawableToBitmapConverter.convertToBitmap(tempDrawable, drawableSize, drawableSize)).generate(),
					applicationInfoBundle,
					extractedColorList);

			applicationList.add(applicationInfoBundle);
		}

		Log.i(TAG, ">>> Number of applications installed: " + applicationList.size());
		LockScreenDataProvider.getInstance(this).setApplicationList(applicationList);
		return extractedColorList;
	}

	protected List<ResolveInfo> readAppInfoFromDevice() {
		Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
		mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);
		return getPackageManager().queryIntentActivities(mainIntent, PackageManager.PERMISSION_GRANTED);
	}

	private GroupColor mapGroupToGroupColor(KMeans.Group group) {
		Color centroid = group.getCentroid();
		GroupColor groupColor = new GroupColor(centroid.getL(), centroid.getA(), centroid.getB());

		boolean isDuplicate;
		for (Color color : group.getColors()) {
			isDuplicate = false;
			ApplicationInfoBundle temp = color.getApplicationInfoBundle();
			ArrayList<ApplicationInfoBundle> tempList = groupColor.getApplicationList();
			int tempListSize = tempList.size();
			for (int i = 0; i < tempListSize; i++) {
				if (temp.getApplicationName().equals(tempList.get(i).getApplicationName())) {
					isDuplicate = true;
					break;
				}
			}
			if (!isDuplicate) {
				groupColor.addApplicationInfoBundle(temp);
			}
		}

		return groupColor;
	}

	private void extractSevenColors(Palette palette, ApplicationInfoBundle applicationInfoBundle, ArrayList<Color> extractedColorList) {
		Palette.Swatch vibrantSwatch = palette.getVibrantSwatch();
		Palette.Swatch lightVibrantSwatch = palette.getLightVibrantSwatch();
		Palette.Swatch darkVibrantSwatch = palette.getDarkVibrantSwatch();

		Palette.Swatch mutedSwatch = palette.getMutedSwatch();
		Palette.Swatch lightMutedSwatch = palette.getLightMutedSwatch();
		Palette.Swatch darkMutedSwatch = palette.getDarkMutedSwatch();

		Log.i(TAG, ">> Starting extracting seven colors from app: " + applicationInfoBundle.getApplicationName());

		if (vibrantSwatch != null) {
			Log.i(TAG, "Extracting vibrant color...");
			extractedColorList.add(makeExtractedColorPoint(vibrantSwatch, applicationInfoBundle));
		}

		if (lightVibrantSwatch != null) {
			Log.i(TAG, "Extracting light vibrant color...");
			extractedColorList.add(makeExtractedColorPoint(lightVibrantSwatch, applicationInfoBundle));
		}

		if (darkVibrantSwatch != null) {
			Log.i(TAG, "Extracting dark vibrant color..");
			extractedColorList.add(makeExtractedColorPoint(darkVibrantSwatch, applicationInfoBundle));
		}

		if (mutedSwatch != null) {
			Log.i(TAG, "Extracting muted color...");
			extractedColorList.add(makeExtractedColorPoint(mutedSwatch, applicationInfoBundle));
		}

		if (lightMutedSwatch != null) {
			Log.i(TAG, "Extracting light muted color...");
			extractedColorList.add(makeExtractedColorPoint(lightMutedSwatch, applicationInfoBundle));
		}

		if (darkMutedSwatch != null) {
			Log.i(TAG, "Extracting dark muted color...");
			extractedColorList.add(makeExtractedColorPoint(darkMutedSwatch, applicationInfoBundle));
		}

		//Log.i(TAG, "Extracting highly populated color...");
		if (palette.getSwatches().size() > 0) {
			Log.d(TAG, applicationInfoBundle.getApplicationName());
			Color color = HighlyPopulatedColorExtractor.extractHighlyPopulatedColor(palette.getSwatches());
			color.setApplicationInfoBundle(applicationInfoBundle);
			extractedColorList.add(color);
		} else {
			Log.d(TAG, "Failed to extract color : " + applicationInfoBundle.getApplicationName());
		}
		//Log.i(TAG, ">> Extraction completed");
	}

	private Color makeExtractedColorPoint(Palette.Swatch swatch, ApplicationInfoBundle applicationInfoBundle) {
		double[] cieLabColor = RGBToCIELabConverter.convertRGBToLab(swatch.getRgb());
		Color color = new Color(cieLabColor[0], cieLabColor[1], cieLabColor[2]);
		color.setApplicationInfoBundle(applicationInfoBundle);
		return color;
	}

}
