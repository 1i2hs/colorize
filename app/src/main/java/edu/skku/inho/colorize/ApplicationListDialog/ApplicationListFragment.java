package edu.skku.inho.colorize.ApplicationListDialog;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import edu.skku.inho.colorize.LockScreenDataProvider;
import edu.skku.inho.colorize.Constants;
import edu.skku.inho.colorize.Keys;
import edu.skku.inho.colorize.R;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link OnApplicationListFragmentInteraction} interface
 * to handle interaction events.
 * Use the {@link ApplicationListFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ApplicationListFragment extends Fragment {
	private static final String TAG = "ApplicationListFragment";

	private String mSelectedColor;
	private int mClickedViewResId;
	private int mApplicationSelectionMode;

	private TextView mGuideTextView;

	private RecyclerView mApplicationListRecyclerView;

	private OnApplicationListFragmentInteraction mListener;

	/**
	 * public constructor must be empty for factory method
	 */
	public ApplicationListFragment() {
		// Required empty public constructor
	}

	/**
	 * @param selectedColor Color selected from the main page.
	 * @param applicationSelectionMode whether to see application list for launching or for selecting shortcuts
	 * @return A new instance of fragment ApplicationListFragment.
	 */
	public static ApplicationListFragment newInstance(String selectedColor, int applicationSelectionMode) {
		ApplicationListFragment fragment = new ApplicationListFragment();
		Bundle args = new Bundle();
		args.putString(Keys.SELECTED_COLOR, selectedColor);
		args.putInt(Keys.APPLICATION_SELECTION_MODE, applicationSelectionMode);
		fragment.setArguments(args);
		return fragment;
	}

	/**
	 *
	 * @param clickedViewResId resource ID for view clicked by user when user try to select shortcut for certain application
	 * @param applicationSelectionMode whether to see application list for launching or for selecting shortcuts
	 * @return
	 */
	public static ApplicationListFragment newInstance(int clickedViewResId, int applicationSelectionMode) {
		ApplicationListFragment fragment = new ApplicationListFragment();
		Bundle args = new Bundle();
		args.putInt(Keys.CLICKED_VIEW_RES_ID, clickedViewResId);
		args.putInt(Keys.APPLICATION_SELECTION_MODE, applicationSelectionMode);
		fragment.setArguments(args);
		return fragment;
	}

	@Override
	public void onAttach(Context context) {
		super.onAttach(context);
		if (context instanceof OnApplicationListFragmentInteraction) {
			mListener = (OnApplicationListFragmentInteraction) context;
		} else {
			throw new RuntimeException(context.toString() + " must implement OnApplicationListFragmentInteraction");
		}
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (getArguments() != null) {
			mApplicationSelectionMode = getArguments().getInt(Keys.APPLICATION_SELECTION_MODE);
			if(mApplicationSelectionMode == Constants.LAUNCH_APPLICATION_MODE) {
				mSelectedColor = getArguments().getString(Keys.SELECTED_COLOR);
			} else {
				mClickedViewResId = getArguments().getInt(Keys.CLICKED_VIEW_RES_ID);
			}
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		// Inflate the layout for this fragment
		return inflater.inflate(R.layout.fragment_application_list, container, false);
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		configureGuideTextView(view);
		configureRecyclerView(view);
	}

	@Override
	public void onDestroyView() {
		super.onDestroyView();
	}

	@Override
	public void onDetach() {
		super.onDetach();
		mListener = null;
	}

	private void configureGuideTextView(View rootLayout) {
		mGuideTextView = (TextView) rootLayout.findViewById(R.id.textView_guide_text);
		String guideText;
		// case: lock screen is running
		if (mApplicationSelectionMode == Constants.LAUNCH_APPLICATION_MODE) {
			guideText = getString(R.string.select_application_to_launch) +
					"(" +
					LockScreenDataProvider.getInstance(getActivity()).getClusterPoint(mSelectedColor).getApplicationList().size() +
					getString(R.string.number_of_applications_found) +
					")";
			// case: setting page
		} else {
			guideText = getString(R.string.select_application_to_use_as_shortcut) +
					"(" +
					LockScreenDataProvider.getInstance(getActivity()).getApplicationList().size() +
					getString(R.string.number_of_applications_found) +
					")";
		}
		mGuideTextView.setText(guideText);
	}

	private void configureRecyclerView(View rootLayout) {
		mApplicationListRecyclerView = (RecyclerView) rootLayout.findViewById(R.id.recyclerView_application_list);
		mApplicationListRecyclerView.setHasFixedSize(true);
		mApplicationListRecyclerView.setLayoutManager(new GridLayoutManager(getActivity(), 3));

		ApplicationListAdapter adapter;
		if(mApplicationSelectionMode == Constants.LAUNCH_APPLICATION_MODE) {
			adapter = new ApplicationListAdapter(LockScreenDataProvider.getInstance(getActivity()).getClusterPoint(mSelectedColor)
					.getApplicationList(), getActivity(), mApplicationSelectionMode);
		} else {
			adapter = new ApplicationListAdapter(LockScreenDataProvider.getInstance(getActivity()).getApplicationList(),
					getActivity(),
					mApplicationSelectionMode,
					mClickedViewResId,
					mListener);
		}
		mApplicationListRecyclerView.setAdapter(adapter);
	}

	public interface OnApplicationListFragmentInteraction {
		void onClickApplicationIcon(String packageName, int clickedViewResId);
	}
}
