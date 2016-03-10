package edu.skku.inho.colorize.SettingPage;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
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
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.TextView;

import edu.skku.inho.colorize.ApplicationInfoBundle;
import edu.skku.inho.colorize.ApplicationListDialog.ApplicationListFragment;
import edu.skku.inho.colorize.Constants;
import edu.skku.inho.colorize.CroppingBackgroundPage.CroppingImageActivity;
import edu.skku.inho.colorize.Keys;
import edu.skku.inho.colorize.LockScreenDataProvider;
import edu.skku.inho.colorize.R;
import edu.skku.inho.colorize.UpdateService;

public class SettingActivity extends AppCompatActivity implements View.OnClickListener, View.OnTouchListener,
		InitializingSettingFragment.InitializingSettingFragmentInteraction,
		CheckingPeriodChoiceDialogFragment.CheckingPeriodChoiceDialogFragmentInteraction,
		ApplicationListFragment.OnApplicationListFragmentInteraction {
	private static final String TAG = "SettingActivity";

	private ImageView mBrandLogoImageView;

	private RadioGroup mGroupingModeSettingRadioGroup;
	private int mSelectedGroupingMode;

	private RelativeLayout mApplicationListChangeCheckPeriodSettingRelativeLayout;
	private TextView mApplicationListChangeCheckPeriodTextView;
	private int mApplicationListChangeCheckingPeriodChoiceIndex = -1;

	private TextView mLockScreenBackgroundSelectionTextView;

	private Switch mApplicationShortcutsUsageSwitch;
	private LinearLayout mApplicationShortcutsUsageSettingLinearLayout;
	private ImageView[] mApplicationShortcutIconImageView = new ImageView[4];
	private ImageView[] mApplicationShortcutIconRemoveImageView = new ImageView[4];
	private boolean mUseApplicationShortcuts = false;
	private ApplicationInfoBundle[] mApplicationShortcuts = new ApplicationInfoBundle[4];

	private Button mStartApplicationButton;
	private Button mStopApplicationButton;
	private boolean mIsLockScreenRunning = false;

	private int mTouchedX = 0;
	private int mTouchedY = 0;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_setting);

		mIsLockScreenRunning = LockScreenDataProvider.getInstance(this).isLockScreenRunning();

		linkViewInstances();
		configureGroupingModeSetting();
		configureApplicationListChangeCheckPeriodSetting();
		configureLockScreenBackgroundSetting();
		configureApplicationShortCutsUsageSetting();
		configureStartApplicationButton();
		configureStopApplicationButton();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
	}

	private void linkViewInstances() {
		mBrandLogoImageView = (ImageView) findViewById(R.id.imageView_brand_icon);

		mGroupingModeSettingRadioGroup = (RadioGroup) findViewById(R.id.radioGroup_grouping_mode_setting);

		mApplicationListChangeCheckPeriodSettingRelativeLayout = (RelativeLayout) findViewById(R.id.relativeLayout_application_list_change_check_period_setting);
		mApplicationListChangeCheckPeriodTextView = (TextView) findViewById(R.id.textView_application_list_change_check_period);

		mLockScreenBackgroundSelectionTextView = (TextView) findViewById(R.id.textView_lock_screen_background_selection);

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

		mStartApplicationButton = (Button) findViewById(R.id.button_start_application);
		mStopApplicationButton = (Button) findViewById(R.id.button_stop_application);
	}

	private void configureGroupingModeSetting() {
		mSelectedGroupingMode = LockScreenDataProvider.getInstance(this).getGroupingMode();
		// check radio button if the mode is already selected in the past
		if (mSelectedGroupingMode != Constants.GROUPING_MODE_NOT_SELECTED) {
			if (mSelectedGroupingMode == Constants.GROUPING_WITH_FIXED_COLOR_MODE) {
				mGroupingModeSettingRadioGroup.check(R.id.radioButton_fixed_color_grouping_mode);
			} else {
				mGroupingModeSettingRadioGroup.check(R.id.radioButton_variable_color_grouping_mode);
			}
		}

		mGroupingModeSettingRadioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(RadioGroup group, int checkedId) {
				if (checkedId == R.id.radioButton_fixed_color_grouping_mode) {
					mSelectedGroupingMode = Constants.GROUPING_WITH_FIXED_COLOR_MODE;
				} else {
					mSelectedGroupingMode = Constants.GROUPING_WITH_VARIABLE_COLOR_MODE;
				}

				if (isGroupingModeChanged()) {
					setStartApplicationButtonEnabled(true);
				} else {
					setStartApplicationButtonEnabled(false);
				}
			}
		});
	}

	private void configureApplicationListChangeCheckPeriodSetting() {
		mApplicationListChangeCheckPeriodSettingRelativeLayout.setOnClickListener(this);
		mApplicationListChangeCheckingPeriodChoiceIndex = LockScreenDataProvider.getInstance(this)
				.getApplicationListChangeCheckingPeriodChoiceIndex();
		mApplicationListChangeCheckPeriodTextView.setText(getResources()
				.getStringArray(R.array.application_list_change_checking_periods)[mApplicationListChangeCheckingPeriodChoiceIndex]);
	}

	private void configureLockScreenBackgroundSetting() {
		mLockScreenBackgroundSelectionTextView.setOnClickListener(this);
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

	private void configureStartApplicationButton() {
		setStartApplicationButtonEnabled(false);

		// if lock screen is running, then stop the LockScreenActivity and UpdateService to restart
		if (mIsLockScreenRunning) {
			mStartApplicationButton.setText(getResources().getString(R.string.restart_lock_screen));
		} else {
			mStartApplicationButton.setText(getResources().getString(R.string.start_lock_screen));
		}
		mStartApplicationButton.setOnClickListener(this);
	}

	private void configureStopApplicationButton() {
		if (mIsLockScreenRunning) {
			mStopApplicationButton.setEnabled(true);
		} else {
			mStopApplicationButton.setEnabled(false);
		}
		mStopApplicationButton.setOnClickListener(this);
	}

	private boolean isGroupingModeChanged() {
		return mSelectedGroupingMode != LockScreenDataProvider.getInstance(this).getGroupingMode();
	}

	private void setStartApplicationButtonEnabled(boolean enabled) {
		mStartApplicationButton.setEnabled(enabled);
		if (enabled) {
			mStartApplicationButton.setBackground(getDrawable(R.drawable.default_enabled_button_background));
		} else {
			mStartApplicationButton.setBackground(getDrawable(R.drawable.default_disabled_button_background));
		}
	}

	@Override
	public void onBackPressed() {
		if (getSupportFragmentManager().getBackStackEntryCount() > 0) {
			Fragment fragment;
			if ((fragment = getSupportFragmentManager().findFragmentByTag("application_list_fragment")) != null) {
				((ApplicationListFragment) fragment).concealApplicationListView();
				return;
			}
		}
		super.onBackPressed();
	}

	@Override
	public void onClick(View v) {
		Log.d(TAG, "onClick");
		Intent updateServiceIntent;
		switch (v.getId()) {
			// case: starting/restarting lock screen
			case R.id.button_start_application:
				updateServiceIntent = new Intent(this, UpdateService.class);

				// save selected options(mode, period, usage of shortcuts) into shared preferences
				LockScreenDataProvider.getInstance(this).setGroupingMode(mSelectedGroupingMode);

				updateServiceIntent.putExtra(Keys.GROUPING_MODE, mSelectedGroupingMode);

				// stop service if there is a running service
				if (mIsLockScreenRunning) {
					stopService(updateServiceIntent);
				}
				// restart service with changed mode
				startService(updateServiceIntent);
				showInitializingSettingFragment();
				break;
			// case: stopping lock screen
			case R.id.button_stop_application:
				updateServiceIntent = new Intent(this, UpdateService.class);
				stopService(updateServiceIntent);

				// change the running state of lock screen into stop state
				mIsLockScreenRunning = false;
				mSelectedGroupingMode = Constants.GROUPING_MODE_NOT_SELECTED;

				LockScreenDataProvider.getInstance(this).setIsLockScreenRunning(mIsLockScreenRunning);
				LockScreenDataProvider.getInstance(this).setGroupingMode(mSelectedGroupingMode);
				mGroupingModeSettingRadioGroup.clearCheck();
				configureStartApplicationButton();
				configureStopApplicationButton();
				break;
			// case: setting checking application list change period
			case R.id.relativeLayout_application_list_change_check_period_setting:
				CheckingPeriodChoiceDialogFragment checkingPeriodChoiceDialogFragment = CheckingPeriodChoiceDialogFragment
						.newInstance(mApplicationListChangeCheckingPeriodChoiceIndex);
				checkingPeriodChoiceDialogFragment.show(getSupportFragmentManager(), "checking_period_choice_dialog_fragment");
				break;
			case R.id.textView_lock_screen_background_selection:
				Intent intent = new Intent(this, CroppingImageActivity.class);
				startActivity(intent);
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

	/**
	 * callback method from CheckingPeriodChoiceDialogFragment
	 */
	@Override
	public void onClickConfirmButton(int index) {
		mApplicationListChangeCheckingPeriodChoiceIndex = index;
		mApplicationListChangeCheckPeriodTextView.setText(getResources()
				.getStringArray(R.array.application_list_change_checking_periods)[mApplicationListChangeCheckingPeriodChoiceIndex]);

		LockScreenDataProvider.getInstance(this).setApplicationListChangeCheckingPeriodChoiceIndex(index);
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
