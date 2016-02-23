package edu.skku.inho.colorize;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class SettingActivity extends AppCompatActivity implements View.OnClickListener,
		InitializingSettingFragment.InitializingSettingFragmentInteraction,
		CheckingPeriodChoiceDialogFragment.CheckingPeriodChoiceDialogFragmentInteraction {
	private static final String TAG = "SettingActivity";

	private SharedPreferences mSharedPreferences;

	private RadioGroup mGroupingModeSettingRadioGroup;
	private int mPreviouslySelectedGroupingMode;
	private int mCurrentlySelectedGroupingMode;

	private RelativeLayout mApplicationListChangeCheckPeriodSettingRelativeLayout;
	private TextView mApplicationListChangeCheckPeriodTextView;
	private int mApplicationListChangeCheckingPeriodChoiceIndex;

	private Button mStartApplicationButton;
	private TextView mStopApplicationTextView;
	private boolean mIsLockScreenRunning = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_setting);

		mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(SettingActivity.this);

		mPreviouslySelectedGroupingMode = mSharedPreferences.getInt(Keys.GROUPING_MODE, Constants.GROUPING_MODE_NOT_SELECTED);
		mCurrentlySelectedGroupingMode = mPreviouslySelectedGroupingMode;
		mIsLockScreenRunning = mSharedPreferences.getBoolean(Keys.IS_LOCK_SCREEN_RUNNING, false);

		linkViewInstances();
		configureGroupingModeSetting();
		configureApplicationListChangeCheckPeriodSetting();
		configureStartApplicationButton();
		configureStopApplicationButton();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
	}

	private void linkViewInstances() {
		mGroupingModeSettingRadioGroup = (RadioGroup) findViewById(R.id.radioGroup_grouping_mode_setting);
		mStartApplicationButton = (Button) findViewById(R.id.button_start_application);
		mStopApplicationTextView = (TextView) findViewById(R.id.textView_stop_application);
		mApplicationListChangeCheckPeriodSettingRelativeLayout = (RelativeLayout) findViewById(R.id.relativeLayout_application_list_change_check_period_setting);
		mApplicationListChangeCheckPeriodTextView = (TextView) findViewById(R.id.textView_application_list_change_check_period);
	}

	private void configureGroupingModeSetting() {
		// check radio button if the mode is already selected in the past
		if (mCurrentlySelectedGroupingMode != Constants.GROUPING_MODE_NOT_SELECTED) {
			if (mCurrentlySelectedGroupingMode == Constants.GROUPING_WITH_FIXED_COLOR_MODE) {
				mGroupingModeSettingRadioGroup.check(R.id.radioButton_fixed_color_grouping_mode);
			} else {
				mGroupingModeSettingRadioGroup.check(R.id.radioButton_variable_color_grouping_mode);
			}
		}

		mGroupingModeSettingRadioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(RadioGroup group, int checkedId) {
				if (checkedId == R.id.radioButton_fixed_color_grouping_mode) {
					mCurrentlySelectedGroupingMode = Constants.GROUPING_WITH_FIXED_COLOR_MODE;
				} else {
					mCurrentlySelectedGroupingMode = Constants.GROUPING_WITH_VARIABLE_COLOR_MODE;
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
		mApplicationListChangeCheckingPeriodChoiceIndex = mSharedPreferences
				.getInt(Keys.CHECKING_PERIOD_INDEX, Constants.DEFAULT_APPLICATION_LIST_CHANGE_CHECKING_PERIOD_TIME_INDEX);
		mApplicationListChangeCheckPeriodTextView.setText(getResources()
				.getStringArray(R.array.application_list_change_checking_periods)[mApplicationListChangeCheckingPeriodChoiceIndex]);
	}

	private void configureStartApplicationButton() {
		setStartApplicationButtonEnabled(false);

		// if lock screen is running, then stop the MainActivity and UpdateService to restart
		if (mIsLockScreenRunning) {
			mStartApplicationButton.setText(getResources().getString(R.string.restart_application));
		} else {
			mStartApplicationButton.setText(getResources().getString(R.string.start_application));
		}
		mStartApplicationButton.setOnClickListener(this);
	}

	private void configureStopApplicationButton() {
		if (mIsLockScreenRunning) {
			mStopApplicationTextView.setEnabled(true);
		} else {
			mStopApplicationTextView.setEnabled(false);
		}
		mStopApplicationTextView.setOnClickListener(this);
	}

	private boolean isGroupingModeChanged() {
		return mPreviouslySelectedGroupingMode != mCurrentlySelectedGroupingMode;
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
	public void onClick(View v) {
		if (v.getId() == R.id.button_start_application) {
			Intent updateServiceIntent = new Intent(this, UpdateService.class);

			mSharedPreferences.edit().putInt(Keys.GROUPING_MODE, mCurrentlySelectedGroupingMode).apply();

			updateServiceIntent.putExtra(Keys.GROUPING_MODE, mCurrentlySelectedGroupingMode);

			// stop service if there is a running service
			if (mIsLockScreenRunning) {
				stopService(updateServiceIntent);
			}
			// restart service with changed mode
			startService(updateServiceIntent);
			showInitializingSettingFragment();
		} else if (v.getId() == R.id.textView_stop_application) {
			Intent updateServiceIntent = new Intent(this, UpdateService.class);
			stopService(updateServiceIntent);

			// TODO stop device SCREEN ON/OFF listener

			// change the running state of lock screen into stop state
			mIsLockScreenRunning = false;
			configureStartApplicationButton();
			//mStartApplicationButton.setEnabled(true);
			setStartApplicationButtonEnabled(true);
			configureStopApplicationButton();
			mPreviouslySelectedGroupingMode = Constants.GROUPING_MODE_NOT_SELECTED;

			mSharedPreferences.edit().putBoolean(Keys.IS_LOCK_SCREEN_RUNNING, mIsLockScreenRunning).apply();
			mSharedPreferences.edit().putInt(Keys.GROUPING_MODE, mPreviouslySelectedGroupingMode).apply();
		} else if (v.getId() == R.id.relativeLayout_application_list_change_check_period_setting) {
			CheckingPeriodChoiceDialogFragment checkingPeriodChoiceDialogFragment = CheckingPeriodChoiceDialogFragment
					.newInstance(mApplicationListChangeCheckingPeriodChoiceIndex);
			checkingPeriodChoiceDialogFragment.show(getSupportFragmentManager(), "checking_period_choice_dialog_fragment");
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
		mSharedPreferences.edit().putInt(Keys.CHECKING_PERIOD_INDEX, mApplicationListChangeCheckingPeriodChoiceIndex).apply();
	}
}
