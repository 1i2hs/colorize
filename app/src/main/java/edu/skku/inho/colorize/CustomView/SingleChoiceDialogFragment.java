package edu.skku.inho.colorize.CustomView;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;

import edu.skku.inho.colorize.R;

/**
 * Created by In-Ho Han on 2/20/16.
 */
public class SingleChoiceDialogFragment extends DialogFragment {
	private static final String TITLE = "title";
	private static final String SELECTION_LIST = "selection_list";
	private static final String SELECTED_CHOICE_INDEX = "selected_choice_index";
	private SingleChoiceDialogFragmentInteraction mListener;

	private int mTitleResId;
	private int mSelectionListResId;
	private int mChoiceIndex;

	public static SingleChoiceDialogFragment newInstance(int titleResId, int selectionListResId, int choiceIndex) {
		SingleChoiceDialogFragment fragment = new SingleChoiceDialogFragment();
		Bundle args = new Bundle();
		args.putInt(TITLE, titleResId);
		args.putInt(SELECTION_LIST, selectionListResId);
		args.putInt(SELECTED_CHOICE_INDEX, choiceIndex);
		fragment.setArguments(args);
		return fragment;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (getArguments() != null) {
			mTitleResId = getArguments().getInt(TITLE);
			mSelectionListResId = getArguments().getInt(SELECTION_LIST);
			mChoiceIndex = getArguments().getInt(SELECTED_CHOICE_INDEX);
		}
	}

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		// Set the dialog title
		builder.setTitle(mTitleResId).setSingleChoiceItems(mSelectionListResId, mChoiceIndex, new DialogInterface.OnClickListener() {
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
		if (context instanceof SingleChoiceDialogFragmentInteraction) {
			mListener = (SingleChoiceDialogFragmentInteraction) context;
		} else {
			throw new RuntimeException(context.toString() + " must implement OnApplicationListFragmentInteraction");
		}
	}

	public interface SingleChoiceDialogFragmentInteraction {
		void onClickConfirmButton(int index);
	}
}
