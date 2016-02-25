package edu.skku.inho.colorize.SettingPage;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;

import edu.skku.inho.colorize.R;

/**
 * Created by XEiN on 2/20/16.
 */
public class CheckingPeriodChoiceDialogFragment extends DialogFragment {
	private static final String SELECTED_CHOICE_INDEX = "selected_choice_index";
	private CheckingPeriodChoiceDialogFragmentInteraction mListener;

	private int mChoiceIndex;

	public static CheckingPeriodChoiceDialogFragment newInstance(int choiceIndex) {
		CheckingPeriodChoiceDialogFragment fragment = new CheckingPeriodChoiceDialogFragment();
		Bundle args = new Bundle();
		args.putInt(SELECTED_CHOICE_INDEX, choiceIndex);
		fragment.setArguments(args);
		return fragment;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (getArguments() != null) {
			mChoiceIndex = getArguments().getInt(SELECTED_CHOICE_INDEX);
		}
	}

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		// Set the dialog title
		builder.setTitle(R.string.application_list_change_checking_period_selection)
				.setSingleChoiceItems(R.array.application_list_change_checking_periods, mChoiceIndex, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						mChoiceIndex = which;
					}
				})
				.setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int id) {
						mListener.onClickConfirmButton(mChoiceIndex);
						dismiss();
					}
				});

		return builder.create();
	}

	@Override
	public void onAttach(Context context) {
		super.onAttach(context);
		if (context instanceof CheckingPeriodChoiceDialogFragmentInteraction) {
			mListener = (CheckingPeriodChoiceDialogFragmentInteraction) context;
		} else {
			throw new RuntimeException(context.toString() + " must implement OnApplicationListFragmentInteraction");
		}
	}

	public interface CheckingPeriodChoiceDialogFragmentInteraction {
		void onClickConfirmButton(int index);
	}
}
