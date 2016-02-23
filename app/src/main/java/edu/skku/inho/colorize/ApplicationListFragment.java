package edu.skku.inho.colorize;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;


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

	private static final String SELECTED_COLOR = "selected_color";

	private String mSelectedColor;

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
	 * @return A new instance of fragment ApplicationListFragment.
	 */
	public static ApplicationListFragment newInstance(String selectedColor) {
		ApplicationListFragment fragment = new ApplicationListFragment();
		Bundle args = new Bundle();
		args.putString(SELECTED_COLOR, selectedColor);
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
			mSelectedColor = getArguments().getString(SELECTED_COLOR);
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

	private void configureRecyclerView(View rootLayout) {
		mApplicationListRecyclerView = (RecyclerView) rootLayout.findViewById(R.id.recyclerView_application_list);
		mApplicationListRecyclerView.setHasFixedSize(true);
		mApplicationListRecyclerView.setLayoutManager(new GridLayoutManager(getActivity(), 3));

		ApplicationListAdapter adapter = new ApplicationListAdapter(ApplicationListProvider.getInstance().getClusterPoint(mSelectedColor)
				.getApplicationList(), getActivity());
		mApplicationListRecyclerView.setAdapter(adapter);
	}

	/**
	 * This interface must be implemented by activities that contain this
	 * fragment to allow an interaction in this fragment to be communicated
	 * to the activity and potentially other fragments contained in that
	 * activity.
	 * <p/>
	 * See the Android Training lesson <a href=
	 * "http://developer.android.com/training/basics/fragments/communicating.html"
	 * >Communicating with Other Fragments</a> for more information.
	 */
	public interface OnApplicationListFragmentInteraction {
		// TODO: Update argument type and name
		void onFragmentInteraction(Uri uri);
	}
}
