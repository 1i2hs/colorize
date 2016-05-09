package edu.skku.inho.colorize.SettingPage;


import android.animation.ObjectAnimator;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import edu.skku.inho.colorize.Constants;
import edu.skku.inho.colorize.Keys;
import edu.skku.inho.colorize.R;


/**
 * Created by In-Ho Han on 2/11/16.
 */
public class InitializingSettingFragment extends Fragment {
	private ProgressBar mInitializingSettingProgressBar;
	private TextView mInitializingSettingProgressTextView;
	private Button mConfirmCompletionOfInitializingSettingButton;

	private InitializingSettingFragmentInteraction mListener;

	private BroadcastReceiver mServiceStateReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			if (intent.getIntExtra(Keys.COLOR_GROUPING_SERVICE_MESSAGE, Constants.COLOR_DATA_NOT_READY) == Constants.COLOR_DATA_READY) {
				mInitializingSettingProgressBar.setVisibility(View.GONE);
				mInitializingSettingProgressTextView.setText(R.string.initializing_setting_done);
				mConfirmCompletionOfInitializingSettingButton.setVisibility(View.VISIBLE);
				mConfirmCompletionOfInitializingSettingButton.setEnabled(true);
				ObjectAnimator.ofFloat(mConfirmCompletionOfInitializingSettingButton, "alpha", 0.0F, 1.0F).setDuration(1000).start();
			}
		}
	};

	public InitializingSettingFragment() {
		// Required empty public constructor
	}

	public static InitializingSettingFragment newInstance() {
		InitializingSettingFragment fragment = new InitializingSettingFragment();
		return fragment;
	}

	@Override
	public void onAttach(Context context) {
		super.onAttach(context);
		if (context instanceof InitializingSettingFragmentInteraction) {
			mListener = (InitializingSettingFragmentInteraction) context;
		} else {
			throw new RuntimeException(context.toString() + " must implement InitializingSettingFragmentInteraction");
		}
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		LocalBroadcastManager.getInstance(getActivity())
				.registerReceiver(mServiceStateReceiver, new IntentFilter(Keys.COLOR_GROUPING_SERVICE_BROADCAST));
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		// Inflate the layout for this fragment
		return inflater.inflate(R.layout.fragment_initializing_setting, container, false);
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		mInitializingSettingProgressBar = (ProgressBar) view.findViewById(R.id.progressBar_initializing_setting);
		mInitializingSettingProgressTextView = (TextView) view.findViewById(R.id.textView_initializing_setting_progess);
		mConfirmCompletionOfInitializingSettingButton = (Button) view.findViewById(R.id.button_confirm_completion);

		mConfirmCompletionOfInitializingSettingButton.setAlpha(0.0F);
		mConfirmCompletionOfInitializingSettingButton.setEnabled(false);
		mConfirmCompletionOfInitializingSettingButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				mListener.onClickConfirmCompletionOfInitializingSettingButton();
			}
		});
	}

	@Override
	public void onDetach() {
		LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(mServiceStateReceiver);
		mListener = null;
		super.onDetach();
	}

	public interface InitializingSettingFragmentInteraction {
		void onClickConfirmCompletionOfInitializingSettingButton();
	}
}
