package edu.skku.inho.colorize.CustomView;

import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import edu.skku.inho.colorize.R;

/**
 * Created by XEiN on 5/3/16.
 */
public class ProgressDialogFragment extends DialogFragment {
	public static String PROGRESS_MESSAGE = "progress_message";

	private String mProgressMessage;

	public static ProgressDialogFragment newInstance(String progressMessage) {
		ProgressDialogFragment progressDialogFragment = new ProgressDialogFragment();
		Bundle args = new Bundle();
		args.putString(PROGRESS_MESSAGE, progressMessage);
		progressDialogFragment.setArguments(args);
		return progressDialogFragment;
	}

	@Override
	public void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		if (getArguments() != null) {
			mProgressMessage = getArguments().getString(PROGRESS_MESSAGE);
		}
	}

	/*@Nullable
	@Override
	public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		//return super.onCreateView(inflater, container, savedInstanceState);
		return inflater.inflate(R.layout.dialogfragment_progress, container, false);
	}*/

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		// Get the layout inflater
		LayoutInflater inflater = getActivity().getLayoutInflater();
		View view = inflater.inflate(R.layout.dialogfragment_progress, null);
		TextView messageTextView = (TextView) view.findViewById(R.id.textView_progress_message);
		messageTextView.setText(mProgressMessage);
		// Inflate and set the layout for the dialog
		// Pass null as the parent view because its going in the dialog layout
		builder.setView(view);
		return builder.create();
	}
}
