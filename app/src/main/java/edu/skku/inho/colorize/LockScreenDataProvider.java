package edu.skku.inho.colorize;

import android.content.Context;
import android.content.pm.PackageManager;
import android.preference.PreferenceManager;
import android.widget.Toast;

import java.util.ArrayList;

import edu.skku.inho.colorize.IconSortingModule.GroupColor;

/**
 * Created by XEiN on 2/11/16.
 */
public class LockScreenDataProvider {
	private static final String TAG = "LockScreenDataProvider";
	private static ArrayList<ApplicationInfoBundle> mApplicationList;
	private static ArrayList<GroupColor> mGroupColorList;
	private static ApplicationInfoBundle[] mApplicationShortcutList = new ApplicationInfoBundle[4];

	private static Context mContext;

	private int mNumberOfGroupColors = Constants.DEFAULT_NUMBER_OF_GROUP_COLOR;
	private boolean mIsLockScreenRunning = false;
	private boolean mIsColorDataReady = false;

	private LockScreenDataProvider() {}

	public static LockScreenDataProvider initInstance(Context context) {
		return getInstance(context);
	}

	public static LockScreenDataProvider getInstance(Context context) {
		if (mContext == null) {
			mContext = context;
		}
		return Singleton.instance;
	}

	public ArrayList<ApplicationInfoBundle> getApplicationList() {
		return mApplicationList;
	}

	public void setApplicationList(ArrayList<ApplicationInfoBundle> applicationList) {
		mApplicationList = applicationList;
	}

	public synchronized ArrayList<GroupColor> getGroupColorList() {
		return mGroupColorList;
	}

	public synchronized void setGroupColorList(ArrayList<GroupColor> groupColorPointList) {
		mGroupColorList = groupColorPointList;
	}

	public void addApplicationShortcut(int index, ApplicationInfoBundle applicationInfoBundle) {
		if (index < 4) {
			mApplicationShortcutList[index] = applicationInfoBundle;
			// caching into shared preferences
			saveIntoSharedPreferences(getApplicationShortcutKey(index), applicationInfoBundle.getApplicationPackageName());
		} else {
			throw new IndexOutOfBoundsException();
		}
	}

	private void saveIntoSharedPreferences(String key, String value) {
		PreferenceManager.getDefaultSharedPreferences(mContext).edit().putString(key, value).apply();
	}

	private String getApplicationShortcutKey(int index) {
		switch (index) {
			case 0:
				return Keys.FIRST_APPLICATION_SHORTCUT_PACKAGE_NAME;
			case 1:
				return Keys.SECOND_APPLICATION_SHORTCUT_PACKAGE_NAME;
			case 2:
				return Keys.THIRD_APPLICATION_SHORTCUT_PACKAGE_NAME;
			case 3:
				return Keys.FOURTH_APPLICATION_SHORTCUT_PACKAGE_NAME;
		}

		throw new IndexOutOfBoundsException();
	}

	public void removeApplicationShortcut(int index) {
		if (index < 4) {
			try {
				// remove data from the shared preferences
				removeFromSharedPreferences(getApplicationShortcut(index).getApplicationPackageName());
				mApplicationShortcutList[index] = null;
			} catch (NullPointerException e) {
				Toast.makeText(mContext, R.string.error_no_data, Toast.LENGTH_SHORT).show();
				e.printStackTrace();
			}
		} else {
			throw new IndexOutOfBoundsException();
		}
	}

	private void removeFromSharedPreferences(String key) {
		PreferenceManager.getDefaultSharedPreferences(mContext).edit().remove(key).apply();
	}

	public ApplicationInfoBundle getApplicationShortcut(int index) {
		if (index < 4) {
			try {
				ApplicationInfoBundle applicationInfoBundle = mApplicationShortcutList[index];
				if (applicationInfoBundle != null) {
					return applicationInfoBundle;
				} else {
					applicationInfoBundle = makeApplicationInfo(getValueFromSharedPreferences(getApplicationShortcutKey(index)));
					if (applicationInfoBundle != null) {
						mApplicationShortcutList[index] = applicationInfoBundle;
						return applicationInfoBundle;
					} else {
						return null;
					}
				}
			} catch (PackageManager.NameNotFoundException e) {
				Toast.makeText(mContext, R.string.error_no_data, Toast.LENGTH_SHORT).show();
				e.printStackTrace();
				return null;
			}
		} else {
			throw new IndexOutOfBoundsException();
		}
	}

	private ApplicationInfoBundle makeApplicationInfo(String packageName) throws PackageManager.NameNotFoundException {
		if (packageName != null) {
			ApplicationInfoBundle applicationInfoBundle = new ApplicationInfoBundle();
			PackageManager packageManager = mContext.getPackageManager();

			applicationInfoBundle.setApplicationName(packageManager
					.getApplicationLabel(packageManager.getApplicationInfo(packageName, PackageManager.GET_META_DATA)).toString());
			applicationInfoBundle.setApplicationPackageName(packageName);
			applicationInfoBundle.setApplicationIcon(packageManager.getApplicationIcon(packageName));

			return applicationInfoBundle;
		} else {
			return null;
		}
	}

	private String getValueFromSharedPreferences(String key) {
		return PreferenceManager.getDefaultSharedPreferences(mContext).getString(key, null);
	}

	public boolean isUseApplicationShortcuts() {
		return PreferenceManager.getDefaultSharedPreferences(mContext).getBoolean(Keys.APPLICATION_SHORTCUTS_USAGE, false);
	}

	public void setUseApplicationShortcuts(boolean useApplicationShortcuts) {
		PreferenceManager.getDefaultSharedPreferences(mContext).edit().putBoolean(Keys.APPLICATION_SHORTCUTS_USAGE, useApplicationShortcuts).apply();
	}

	private void saveIntoSharedPreferences(String key, int value) {
		PreferenceManager.getDefaultSharedPreferences(mContext).edit().putInt(key, value).apply();
	}

	private void saveIntoSharedPreferences(String key, boolean value) {
		PreferenceManager.getDefaultSharedPreferences(mContext).edit().putBoolean(key, value).apply();
	}

	public GroupColor getClusterPoint(String index) {
		switch (index) {
			case GroupColor.FIRST_COLOR:
				return mGroupColorList.get(0);
			case GroupColor.SECOND_COLOR:
				return mGroupColorList.get(1);
			case GroupColor.THIRD_COLOR:
				return mGroupColorList.get(2);
			case GroupColor.FOURTH_COLOR:
				return mGroupColorList.get(3);
			case GroupColor.FIFTH_COLOR:
				return mGroupColorList.get(4);
			case GroupColor.SIXTH_COLOR:
				return mGroupColorList.get(5);
			case GroupColor.SEVENTH_COLOR:
				return mGroupColorList.get(6);
			case GroupColor.EIGHTH_COLOR:
				return mGroupColorList.get(7);
			default:
				return null;
		}
	}

	public int getNumberOfGroupColors() {
		return mNumberOfGroupColors;
	}

	public void setNumberOfGroupColors(int numberOfGroupColors) {
		mNumberOfGroupColors = numberOfGroupColors;
	}

	public boolean isLockScreenRunning() {
		return mIsLockScreenRunning;
	}

	public void setIsLockScreenRunning(boolean isLockScreenRunning) {
		mIsLockScreenRunning = isLockScreenRunning;
	}

	public boolean isColorDataReady() {
		return mIsColorDataReady;
	}

	public void setIsColorDataReady(boolean isColorDataReady) {
		mIsColorDataReady = isColorDataReady;
	}

	public int getGroupingMode() {
		return PreferenceManager.getDefaultSharedPreferences(mContext).getInt(Keys.GROUPING_MODE, Constants.GROUPING_MODE_NOT_SELECTED);
	}

	public void setGroupingMode(int groupingMode) {
		PreferenceManager.getDefaultSharedPreferences(mContext).edit().putInt(Keys.GROUPING_MODE, groupingMode).apply();
	}

	public int getApplicationListChangeCheckingPeriodChoiceIndex() {
		return PreferenceManager.getDefaultSharedPreferences(mContext)
				.getInt(Keys.CHECKING_PERIOD_INDEX, Constants.DEFAULT_APPLICATION_LIST_CHANGE_CHECKING_PERIOD_TIME_INDEX);
	}

	public void setApplicationListChangeCheckingPeriodChoiceIndex(int index) {
		PreferenceManager.getDefaultSharedPreferences(mContext).edit().putInt(Keys.CHECKING_PERIOD_INDEX, index).apply();
	}

	private static class Singleton {
		private static final LockScreenDataProvider instance = new LockScreenDataProvider();
	}
}
