package edu.skku.inho.colorize.SettingPage;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.Transformation;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import edu.skku.inho.colorize.ApplicationInfoBundle;
import edu.skku.inho.colorize.ApplicationListDialog.ApplicationListFragment;
import edu.skku.inho.colorize.ColorGroupingService;
import edu.skku.inho.colorize.Constants;
import edu.skku.inho.colorize.CroppingBackgroundPage.CroppingImageActivity;
import edu.skku.inho.colorize.CustomView.ProgressDialogFragment;
import edu.skku.inho.colorize.CustomView.SingleChoiceDialogFragment;
import edu.skku.inho.colorize.IconGroupingModule.DrawableToBitmapConverter;
import edu.skku.inho.colorize.Keys;
import edu.skku.inho.colorize.LockScreenDataProvider;
import edu.skku.inho.colorize.R;
import edu.skku.inho.colorize.TextColorSettingPage.TextColorSettingActivity;

public class SettingActivity extends AppCompatActivity implements View.OnClickListener, View.OnTouchListener,
		InitializingSettingFragment.InitializingSettingFragmentInteraction, SingleChoiceDialogFragment.SingleChoiceDialogFragmentInteraction,
		ApplicationListFragment.OnApplicationListFragmentInteraction {
	private static final String TAG = "SettingActivity";

	private ColorGroupingService mColorGroupingService;
	private boolean mIsBoundToService = false;

	private ImageView mBrandLogoImageView;

	private TextView mLockScreenBackgroundSelectionTextView;

	private TextView mLockScreenDigitalClockTextColorSelectionTextView;

	private Switch mApplicationShortcutsUsageSwitch;
	private LinearLayout mApplicationShortcutsUsageSettingLinearLayout;
	private ImageView[] mApplicationShortcutIconImageView = new ImageView[4];
	private ImageView[] mApplicationShortcutIconRemoveImageView = new ImageView[4];
	private boolean mUseApplicationShortcuts = false;
	private ApplicationInfoBundle[] mApplicationShortcuts = new ApplicationInfoBundle[4];

	private Button mToggleApplicationButton;
	private boolean mIsLockScreenRunning = false;

	private int mTouchedX = 0;
	private int mTouchedY = 0;

	private LinearLayout mGroupingModeSelectionLinearLayout;
	private TextView mSelectedGroupingModeTextView;
	private int mSelectedGroupingMode;

	private ProgressDialogFragment mProgressDialogFragment;

	private BroadcastReceiver mServiceStateReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			if (intent.getIntExtra(Keys.COLOR_GROUPING_SERVICE_MESSAGE, Constants.COLOR_DATA_NOT_READY) == Constants.COLOR_DATA_READY) {
				if (mProgressDialogFragment != null) {
					mProgressDialogFragment.dismiss();
				}
			}
		}
	};
	private ServiceConnection mServiceConnection = new ServiceConnection() {
		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			Log.i(TAG, "connected with the service");
			ColorGroupingService.ColorGroupingLocalBinder binder = (ColorGroupingService.ColorGroupingLocalBinder) service;
			mColorGroupingService = binder.getService();
			mIsBoundToService = true;
		}

		@Override
		public void onServiceDisconnected(ComponentName name) {
			Log.i(TAG, "disconnected with the service");
			mIsBoundToService = false;
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_setting);

		mIsLockScreenRunning = LockScreenDataProvider.getInstance(this).isLockScreenRunning();

		LocalBroadcastManager.getInstance(this).registerReceiver(mServiceStateReceiver, new IntentFilter(Keys.COLOR_GROUPING_SERVICE_BROADCAST));

		if (!doesBackgroundImageFileExist()) {
			createDefaultBackgroundImageFile();
		}

		linkViewInstances();
		configureGroupingModeSetting();
		configureLockScreenBackgroundSetting();
		configureLockScreenDigitalClockFontSetting();
		configureApplicationShortCutsUsageSetting();
		configureToggleApplicationButton();
	}

	@Override
	protected void onStop() {
		super.onStop();
		// unbind the activity from the service
		if (mIsBoundToService) {
			unbindService(mServiceConnection);
			mIsBoundToService = false;
		}
	}

	@Override
	protected void onDestroy() {
		LocalBroadcastManager.getInstance(this).unregisterReceiver(mServiceStateReceiver);
		super.onDestroy();
	}

	private boolean doesBackgroundImageFileExist() {
		File imageFile = new File(getDir(getResources().getString(R.string.background_image_file_dir_name), ContextWrapper.MODE_PRIVATE),
				getResources().getString(R.string.background_image_file_name));
		if (imageFile.exists() && !imageFile.isDirectory()) {
			return true;
		} else {
			return false;
		}
	}

	private void createDefaultBackgroundImageFile() {
		File backgroundImageFilePath = getDir(getResources().getString(R.string.background_image_file_dir_name), ContextWrapper.MODE_PRIVATE);
		File backgroundImageFile = new File(backgroundImageFilePath, getResources().getString(R.string.background_image_file_name));
		FileOutputStream out = null;

		Bitmap defaultBackgroundBitmap = DrawableToBitmapConverter.convertToBitmap(getResources().getDrawable(R.mipmap.colorize_background));

		try {
			backgroundImageFile.createNewFile();
			out = new FileOutputStream(backgroundImageFile);

			defaultBackgroundBitmap.compress(Bitmap.CompressFormat.PNG, 100, out);

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				out.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	private void linkViewInstances() {
		mBrandLogoImageView = (ImageView) findViewById(R.id.imageView_brand_icon);

		mLockScreenBackgroundSelectionTextView = (TextView) findViewById(R.id.textView_lock_screen_background_selection);

		mLockScreenDigitalClockTextColorSelectionTextView = (TextView) findViewById(R.id.textView_lock_screen_digital_clock_text_color_selection);

		mApplicationShortcutsUsageSwitch = (Switch) findViewById(R.id.switch_application_shortcuts_usage);
		mApplicationShortcutsUsageSettingLinearLayout = (LinearLayout) findViewById(R.id.linearLayout_application_shortcuts);
		mApplicationShortcutIconImageView[0] = (ImageView) findViewById(R.id.imageView_first_shortcut_app);
		mApplicationShortcutIconImageView[1] = (ImageView) findViewById(R.id.imageView_second_shortcut_app);
		mApplicationShortcutIconImageView[2] = (ImageView) findViewById(R.id.imageView_third_shortcut_app);
		mApplicationShortcutIconImageView[3] = (ImageView) findViewById(R.id.imageView_fourth_shortcut_app);
		mApplicationShortcutIconRemoveImageView[0] = (ImageView) findViewById(R.id.imageView_remove_first_shortcut_app);
		mApplicationShortcutIconRemoveImageView[1] = (ImageView) findViewById(R.id.imageView_remove_second_shortcut_app);
		mApplicationShortcutIconRemoveImageView[2] = (ImageView) findViewById(R.id.imageView_remove_third_shortcut_app);
		mApplicationShortcutIconRemoveImageView[3] = (ImageView) findViewById(R.id.imageView_remove_fourth_shortcut_app);

		mGroupingModeSelectionLinearLayout = (LinearLayout) findViewById(R.id.linearLayout_grouping_mode_selection);
		mSelectedGroupingModeTextView = (TextView) findViewById(R.id.textView_selected_grouping_mode);

		mToggleApplicationButton = (Button) findViewById(R.id.button_toggle_application);
	}

	private void configureGroupingModeSetting() {
		mSelectedGroupingMode = LockScreenDataProvider.getInstance(this).getGroupingMode();
		setGroupingModeText();
		mGroupingModeSelectionLinearLayout.setOnClickListener(this);
	}

	private void configureLockScreenBackgroundSetting() {
		mLockScreenBackgroundSelectionTextView.setOnClickListener(this);
	}

	private void configureLockScreenDigitalClockFontSetting() {
		mLockScreenDigitalClockTextColorSelectionTextView.setOnClickListener(this);
	}

	private void configureApplicationShortCutsUsageSetting() {
		LockScreenDataProvider lockScreenDataProvider = LockScreenDataProvider.getInstance(this);
		mUseApplicationShortcuts = lockScreenDataProvider.isUseApplicationShortcuts();
		mApplicationShortcutsUsageSwitch.setChecked(mUseApplicationShortcuts);

		for (int i = 0; i < Constants.NUMBER_OF_APPLICATION_SHORTCUTS; i++) {
			if ((mApplicationShortcuts[i] = lockScreenDataProvider.getApplicationShortcut(i)) != null) {
				mApplicationShortcutIconImageView[i].setImageDrawable(mApplicationShortcuts[i].getApplicationIcon());
				mApplicationShortcutIconRemoveImageView[i].setVisibility(View.VISIBLE);
			}
			mApplicationShortcutIconImageView[i].setOnTouchListener(this);
			mApplicationShortcutIconRemoveImageView[i].setOnClickListener(this);
		}

		// case : application shortcuts usage is already set before
		if (mUseApplicationShortcuts) {
			mApplicationShortcutsUsageSettingLinearLayout.getLayoutParams().height = (int) TypedValue
					.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 70, getResources().getDisplayMetrics());
			mApplicationShortcutsUsageSettingLinearLayout.requestLayout();
		}

		mApplicationShortcutsUsageSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				final int targetHeight;
				final int originalHeight;
				if (isChecked) {
					targetHeight = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 70, getResources().getDisplayMetrics());
					originalHeight = 0;
				} else {
					originalHeight = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 70, getResources().getDisplayMetrics());
					targetHeight = 0;
				}

				Animation animation = new ApplicationShortcutsLinearLayoutTransitionAnimation(originalHeight,
						targetHeight,
						mApplicationShortcutsUsageSettingLinearLayout);
				animation.setDuration(300);
				mApplicationShortcutsUsageSettingLinearLayout.startAnimation(animation);

				mUseApplicationShortcuts = isChecked;
				LockScreenDataProvider.getInstance(SettingActivity.this).setUseApplicationShortcuts(isChecked);
			}
		});
	}

	private void configureToggleApplicationButton() {
		toggleToggleApplicationButton();
		mToggleApplicationButton.setOnClickListener(this);
	}

	private void setGroupingModeText() {
		if (mSelectedGroupingMode == Constants.GROUPING_WITH_FIXED_COLOR_MODE) {
			mSelectedGroupingModeTextView.setText(R.string.grouping_with_fixed_color_mode);
		} else {
			mSelectedGroupingModeTextView.setText(R.string.grouping_with_variable_color_mode);
		}
	}

	private void toggleToggleApplicationButton() {
		// if lock screen is running, then stop the LockScreenActivity and ColorGroupingService to restart
		if (mIsLockScreenRunning) {
			mToggleApplicationButton.setBackground(getDrawable(R.drawable.default_disabled_button_background));
			mToggleApplicationButton.setText(getResources().getString(R.string.stop_lock_screen));
		} else {
			mToggleApplicationButton.setBackground(getDrawable(R.drawable.default_enabled_button_background));
			mToggleApplicationButton.setText(getResources().getString(R.string.start_lock_screen));
		}
	}

	@Override
	public void onClickConfirmButton(int index) {
		mSelectedGroupingMode = index;
		if (isGroupingModeChanged()) {
			setGroupingModeText();
			// save selected options(mode, period, usage of shortcuts) into shared preferences
			LockScreenDataProvider.getInstance(this).setGroupingMode(mSelectedGroupingMode);

			//if the service is running already, which also means the lock screen is running,
			//compute the group color again immediately without clicking start application button.
			if (mIsLockScreenRunning && mIsBoundToService) {
				mProgressDialogFragment = ProgressDialogFragment.newInstance(getResources().getString(R.string.changing_mode));
				mProgressDialogFragment.show(getSupportFragmentManager(), null);
				mColorGroupingService.computeGroupColors();
			}
		}
	}

	private boolean isGroupingModeChanged() {
		return mSelectedGroupingMode != LockScreenDataProvider.getInstance(this).getGroupingMode();
	}

	@Override
	public void onBackPressed() {
		if (getSupportFragmentManager().getBackStackEntryCount() > 0) {
			Fragment fragment;
			if ((fragment = getSupportFragmentManager().findFragmentByTag("application_list_fragment")) != null) {
				((ApplicationListFragment) fragment).concealApplicationList();
				return;
			}
		}
		super.onBackPressed();
	}

	@Override
	protected void onStart() {
		super.onStart();
		// bind the activity to the service
		if (mIsLockScreenRunning) {
			Intent intent = new Intent(this, ColorGroupingService.class);
			bindService(intent, mServiceConnection, 0);
		}
	}

	@Override
	public void onClick(View v) {
		Log.d(TAG, "onClick");
		Intent groupingColorServiceIntent;
		switch (v.getId()) {
			// case: starting/restarting lock screen
			case R.id.button_toggle_application:
				groupingColorServiceIntent = new Intent(this, ColorGroupingService.class);

				// save selected options(mode, period, usage of shortcuts) into shared preferences
				LockScreenDataProvider.getInstance(this).setGroupingMode(mSelectedGroupingMode);

				// stop service if there is a running service
				if (mIsLockScreenRunning) {
					stopService(groupingColorServiceIntent);
					// change the running state of lock screen into stop state
					mIsLockScreenRunning = false;

					LockScreenDataProvider.getInstance(this).setIsLockScreenRunning(mIsLockScreenRunning);
					LockScreenDataProvider.getInstance(this).setGroupingMode(mSelectedGroupingMode);
					toggleToggleApplicationButton();
				} else {
					// restart service with changed mode
					startService(groupingColorServiceIntent);
					showInitializingSettingFragment();
				}
				break;
			case R.id.linearLayout_grouping_mode_selection:
				SingleChoiceDialogFragment singleChoiceDialogFragment = SingleChoiceDialogFragment.newInstance(R.string.group_mode_title,
						R.array.grouping_mode_list,
						LockScreenDataProvider.getInstance(this).getGroupingMode());
				singleChoiceDialogFragment.show(getSupportFragmentManager(), "grouping_mode_selection_dialog_fragment");
				break;
			case R.id.textView_lock_screen_background_selection:
				Intent croppingImageActivityIntent = new Intent(this, CroppingImageActivity.class);
				startActivity(croppingImageActivityIntent);
				break;
			case R.id.textView_lock_screen_digital_clock_text_color_selection:
				Intent textColorSettingActivityIntent = new Intent(this, TextColorSettingActivity.class);
				startActivity(textColorSettingActivityIntent);
				break;
			case R.id.imageView_remove_first_shortcut_app:
				removeApplicationShortcut(Constants.FIRST_APPLICATION_SHORTCUT);
				break;
			case R.id.imageView_remove_second_shortcut_app:
				removeApplicationShortcut(Constants.SECOND_APPLICATION_SHORTCUT);
				break;
			case R.id.imageView_remove_third_shortcut_app:
				removeApplicationShortcut(Constants.THIRD_APPLICATION_SHORTCUT);
				break;
			case R.id.imageView_remove_fourth_shortcut_app:
				removeApplicationShortcut(Constants.FOURTH_APPLICATION_SHORTCUT);
				break;
		}
	}

	private void showInitializingSettingFragment() {
		FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
		fragmentTransaction.add(android.R.id.content, InitializingSettingFragment.newInstance());
		fragmentTransaction.setCustomAnimations(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
		fragmentTransaction.commit();
	}

	private void removeApplicationShortcut(int applicationShortcutIndex) {
		mApplicationShortcutIconImageView[applicationShortcutIndex].setImageResource(R.drawable.ic_add_circle_outline_black_48dp);
		mApplicationShortcutIconRemoveImageView[applicationShortcutIndex].setVisibility(View.INVISIBLE);

		LockScreenDataProvider.getInstance(this).removeApplicationShortcut(applicationShortcutIndex);
	}

	@Override
	public boolean onTouch(View v, MotionEvent event) {
		if (event.getAction() == MotionEvent.ACTION_DOWN) {
			mTouchedX = (int) event.getRawX();
			mTouchedY = (int) event.getRawY();
		}

		if (event.getAction() == MotionEvent.ACTION_UP) {
			showShortcutSelectionApplicationListFragment(v);
		}
		return true;
	}

	private void showShortcutSelectionApplicationListFragment(View clickedView) {
		ApplicationListFragment applicationListFragment = ApplicationListFragment
				.newInstance(clickedView.getId(), Constants.SELECT_SHORTCUT_APPLICATION_MODE, mTouchedX, mTouchedY);
		FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
		fragmentTransaction.add(android.R.id.content, applicationListFragment, "application_list_fragment");
		fragmentTransaction.addToBackStack(null);
		fragmentTransaction.commit();
	}

	@Override
	public void onClickConfirmCompletionOfInitializingSettingButton() {
		getFragmentManager().popBackStack();
		finish();
	}

	@Override
	public void onClickApplicationIcon(String packageName, int clickedViewResId) {
		try {
			switch (clickedViewResId) {
				case R.id.imageView_first_shortcut_app:
					addApplicationShortcut(Constants.FIRST_APPLICATION_SHORTCUT, packageName);
					break;
				case R.id.imageView_second_shortcut_app:
					addApplicationShortcut(Constants.SECOND_APPLICATION_SHORTCUT, packageName);
					break;
				case R.id.imageView_third_shortcut_app:
					addApplicationShortcut(Constants.THIRD_APPLICATION_SHORTCUT, packageName);
					break;
				case R.id.imageView_fourth_shortcut_app:
					addApplicationShortcut(Constants.FOURTH_APPLICATION_SHORTCUT, packageName);
					break;
			}
		} catch (PackageManager.NameNotFoundException e) {
			e.printStackTrace();
		}
	}

	private void addApplicationShortcut(int applicationShortcutIndex, String packageName) throws PackageManager.NameNotFoundException {
		ApplicationInfoBundle applicationInfoBundle = new ApplicationInfoBundle();
		Drawable applicationIcon = getPackageManager().getApplicationIcon(packageName);

		applicationInfoBundle.setApplicationIcon(applicationIcon);
		applicationInfoBundle.setApplicationPackageName(packageName);

		mApplicationShortcutIconImageView[applicationShortcutIndex].setImageDrawable(applicationIcon);
		mApplicationShortcutIconRemoveImageView[applicationShortcutIndex].setVisibility(View.VISIBLE);

		LockScreenDataProvider.getInstance(this).addApplicationShortcut(applicationShortcutIndex, applicationInfoBundle);
	}

	private class ApplicationShortcutsLinearLayoutTransitionAnimation extends Animation {
		private int mOriginalHeight;
		private int mTargetHeight;
		private View mTargetView;

		public ApplicationShortcutsLinearLayoutTransitionAnimation(int originalHeight, int targetHeight, View targetView) {
			mOriginalHeight = originalHeight;
			mTargetHeight = targetHeight;
			mTargetView = targetView;
		}

		@Override
		protected void applyTransformation(float interpolatedTime, Transformation t) {
			//super.applyTransformation(interpolatedTime, t);
			final int currentHeight = mOriginalHeight + (int) ((mTargetHeight - mOriginalHeight) * interpolatedTime);
			mTargetView.getLayoutParams().height = currentHeight;
			mTargetView.requestLayout();
		}
	}
}
