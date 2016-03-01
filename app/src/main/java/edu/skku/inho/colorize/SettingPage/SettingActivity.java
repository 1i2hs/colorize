package edu.skku.inho.colorize.SettingPage;

import android.content.Intent;
import android.content.pm.PackageManager;
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
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.TextView;

import edu.skku.inho.colorize.ApplicationInfoBundle;
import edu.skku.inho.colorize.ApplicationListDialog.ApplicationListFragment;
import edu.skku.inho.colorize.Constants;
import edu.skku.inho.colorize.Keys;
import edu.skku.inho.colorize.LockScreenDataProvider;
import edu.skku.inho.colorize.R;
import edu.skku.inho.colorize.RoundView;
import edu.skku.inho.colorize.UpdateService;

public class SettingActivity extends AppCompatActivity implements View.OnClickListener,
		InitializingSettingFragment.InitializingSettingFragmentInteraction,
		CheckingPeriodChoiceDialogFragment.CheckingPeriodChoiceDialogFragmentInteraction,
		ApplicationListFragment.OnApplicationListFragmentInteraction {
	private static final String TAG = "SettingActivity";

	private RadioGroup mGroupingModeSettingRadioGroup;
	private int mSelectedGroupingMode;

	private RelativeLayout mApplicationListChangeCheckPeriodSettingRelativeLayout;
	private TextView mApplicationListChangeCheckPeriodTextView;
	private int mApplicationListChangeCheckingPeriodChoiceIndex = -1;

	private Switch mApplicationShortcutsUsageSwitch;
	private LinearLayout mApplicationShortcutsUsageSettingLinearLayout;
	private RoundView[] mApplicationShortcutIconRoundView = new RoundView[4];
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
		configureApplicationShortCutsUsageSetting();
		configureStartApplicationButton();
		configureStopApplicationButton();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
	}

	private void linkViewInstances() {
		mGroupingModeSettingRadioGroup = (RadioGroup) findViewById(R.id.radioGroup_grouping_mode_setting);

		mApplicationListChangeCheckPeriodSettingRelativeLayout = (RelativeLayout) findViewById(R.id.relativeLayout_application_list_change_check_period_setting);
		mApplicationListChangeCheckPeriodTextView = (TextView) findViewById(R.id.textView_application_list_change_check_period);

		mApplicationShortcutsUsageSwitch = (Switch) findViewById(R.id.switch_application_shortcuts_usage);
		mApplicationShortcutsUsageSettingLinearLayout = (LinearLayout) findViewById(R.id.linearLayout_application_shortcuts);
		mApplicationShortcutIconRoundView[0] = (RoundView) findViewById(R.id.view_first_shortcut_app);
		mApplicationShortcutIconRoundView[1] = (RoundView) findViewById(R.id.view_second_shortcut_app);
		mApplicationShortcutIconRoundView[2] = (RoundView) findViewById(R.id.view_third_shortcut_app);
		mApplicationShortcutIconRoundView[3] = (RoundView) findViewById(R.id.view_fourth_shortcut_app);

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

	private void configureApplicationShortCutsUsageSetting() {
		LockScreenDataProvider lockScreenDataProvider = LockScreenDataProvider.getInstance(this);
		mUseApplicationShortcuts = lockScreenDataProvider.isUseApplicationShortcuts();
		mApplicationShortcutsUsageSwitch.setChecked(mUseApplicationShortcuts);

		for (int i = 0; i < Constants.NUMBER_OF_APPLICATION_SHORTCUTS; i++) {
			if ((mApplicationShortcuts[i] = lockScreenDataProvider.getApplicationShortcut(i)) != null) {
				mApplicationShortcutIconRoundView[i].setBackground(mApplicationShortcuts[i].getApplicationIcon());
			}
			//mApplicationShortcutIconRoundView[i].setOnClickListener(this);
			mApplicationShortcutIconRoundView[i].setOnTouchListener(new View.OnTouchListener() {
				@Override
				public boolean onTouch(View v, MotionEvent event) {
					if (event.getAction() == MotionEvent.ACTION_DOWN) {
						mTouchedX = (int) event.getRawX();
						mTouchedY = (int) event.getRawY();
						Log.d(TAG, "Touched x, y = " + mTouchedX + ", " + mTouchedY);
					}

					if (event.getAction() == MotionEvent.ACTION_UP) {
						showShortcutSelectionApplicationListFragment(v);
					}
					return true;
				}
			});
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

		// if lock screen is running, then stop the MainActivity and UpdateService to restart
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

	private void showShortcutSelectionApplicationListFragment(View clickedView) {
		ApplicationListFragment applicationListFragment = ApplicationListFragment
				.newInstance(clickedView.getId(), Constants.SELECT_SHORTCUT_APPLICATION_MODE, mTouchedX, mTouchedY);
		FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
		fragmentTransaction.add(android.R.id.content, applicationListFragment, "application_list_fragment");
		fragmentTransaction.addToBackStack(null);
		fragmentTransaction.commit();
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
			case R.id.view_first_shortcut_app:
			case R.id.view_second_shortcut_app:
			case R.id.view_third_shortcut_app:
			case R.id.view_fourth_shortcut_app:
				showShortcutSelectionApplicationListFragment(v);
				break;
		}
	}

	private void showInitializingSettingFragment() {
		FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
		fragmentTransaction.add(android.R.id.content, InitializingSettingFragment.newInstance());
		fragmentTransaction.setCustomAnimations(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
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
			ApplicationInfoBundle applicationInfoBundle = new ApplicationInfoBundle();
			applicationInfoBundle.setApplicationIcon(getPackageManager().getApplicationIcon(packageName));
			applicationInfoBundle.setApplicationPackageName(packageName);

			switch (clickedViewResId) {
				case R.id.view_first_shortcut_app:
					mApplicationShortcutIconRoundView[Constants.FIRST_APPLICATION_SHORTCUT]
							.setBackground(getPackageManager().getApplicationIcon(packageName));
					LockScreenDataProvider.getInstance(this).addApplicationShortcut(Constants.FIRST_APPLICATION_SHORTCUT, applicationInfoBundle);
					break;
				case R.id.view_second_shortcut_app:
					mApplicationShortcutIconRoundView[Constants.SECOND_APPLICATION_SHORTCUT]
							.setBackground(getPackageManager().getApplicationIcon(packageName));
					LockScreenDataProvider.getInstance(this).addApplicationShortcut(Constants.SECOND_APPLICATION_SHORTCUT, applicationInfoBundle);
					break;
				case R.id.view_third_shortcut_app:
					mApplicationShortcutIconRoundView[Constants.THIRD_APPLICATION_SHORTCUT]
							.setBackground(getPackageManager().getApplicationIcon(packageName));
					LockScreenDataProvider.getInstance(this).addApplicationShortcut(Constants.THIRD_APPLICATION_SHORTCUT, applicationInfoBundle);
					break;
				case R.id.view_fourth_shortcut_app:
					mApplicationShortcutIconRoundView[Constants.FOURTH_APPLICATION_SHORTCUT]
							.setBackground(getPackageManager().getApplicationIcon(packageName));
					LockScreenDataProvider.getInstance(this).addApplicationShortcut(Constants.FOURTH_APPLICATION_SHORTCUT, applicationInfoBundle);
					break;
			}
		} catch (PackageManager.NameNotFoundException e) {
			e.printStackTrace();
		}
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
