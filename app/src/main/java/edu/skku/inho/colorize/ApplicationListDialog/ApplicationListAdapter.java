package edu.skku.inho.colorize.ApplicationListDialog;

import android.app.Activity;
import android.graphics.drawable.Drawable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

import edu.skku.inho.colorize.ApplicationInfoBundle;
import edu.skku.inho.colorize.Constants;
import edu.skku.inho.colorize.R;

/**
 * Created by XEiN on 1/27/16.
 */
public class ApplicationListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
	private static final String TAG = "ApplicationListAdapter";

	private Activity mActivity;

	private ArrayList<ApplicationInfoBundle> mApplicationInfoBundleArrayList;

	private int mApplicationSelectionMode = -1;

	private int mClickedViewResId = -1;

	private ApplicationListFragment.OnApplicationListFragmentInteraction mOnApplicationListFragmentInteraction;

	public ApplicationListAdapter(ArrayList<ApplicationInfoBundle> applicationInfoBundleArrayList, Activity activity, int applicationSelectionMode) {
		mApplicationInfoBundleArrayList = applicationInfoBundleArrayList;
		mActivity = activity;
		mApplicationSelectionMode = applicationSelectionMode;
	}

	public ApplicationListAdapter(ArrayList<ApplicationInfoBundle> applicationInfoBundleArrayList, Activity activity, int applicationSelectionMode, int clickedViewResId, ApplicationListFragment.OnApplicationListFragmentInteraction onApplicationListFragmentInteraction) {
		mApplicationInfoBundleArrayList = applicationInfoBundleArrayList;
		mApplicationSelectionMode = applicationSelectionMode;
		mActivity = activity;
		mClickedViewResId = clickedViewResId;
		mOnApplicationListFragmentInteraction = onApplicationListFragmentInteraction;
	}

	@Override
	public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
		View applicationIconView = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_application_icon_name, parent, false);
		if (mApplicationSelectionMode == Constants.LAUNCH_APPLICATION_MODE) {
			return new ApplicationIconViewHolder(applicationIconView, mActivity, mApplicationSelectionMode);
		} else {
			return new ApplicationIconViewHolder(applicationIconView,
					mActivity,
					mApplicationSelectionMode,
					mClickedViewResId,
					mOnApplicationListFragmentInteraction);
		}
	}

	@Override
	public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, int position) {
		ApplicationInfoBundle temp = mApplicationInfoBundleArrayList.get(position);

		((ApplicationIconViewHolder) viewHolder).setIconImage(temp.getApplicationIcon());
		//((ApplicationIconViewHolder) viewHolder).setIconImage(mPackageManager.getApplicationIcon(temp.getApplicationPackageName()));
		((ApplicationIconViewHolder) viewHolder).setNameText(temp.getApplicationName());
		((ApplicationIconViewHolder) viewHolder).setPackageName(temp.getApplicationPackageName());
		//((ApplicationIconViewHolder) viewHolder).setIntentForPackage(temp.getIntentForPackage());
		((ApplicationIconViewHolder) viewHolder).configureOnClickListener();

	}

	@Override
	public int getItemCount() {
		return mApplicationInfoBundleArrayList.size();
	}

	public static class ApplicationIconViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
		private View mViewHolder;
		private Activity mActivity;
		private ImageView mApplicationIconImageView;
		private TextView mApplicationNameTextView;
		//private Intent mIntentForPackage;
		private String mPackageName;
		private int mClickedViewResId;
		private int mApplicationSelectionMode;
		private ApplicationListFragment.OnApplicationListFragmentInteraction mOnApplicationListFragmentInteraction;

		public ApplicationIconViewHolder(View itemView, Activity activity, int applicationSelectionMode) {
			super(itemView);
			mViewHolder = itemView;
			mActivity = activity;
			mApplicationIconImageView = (ImageView) itemView.findViewById(R.id.imageView_application_icon);
			mApplicationNameTextView = (TextView) itemView.findViewById(R.id.textView_application_name);
			mApplicationSelectionMode = applicationSelectionMode;
		}

		public ApplicationIconViewHolder(View itemView, Activity activity, int applicationSelectionMode, int clickedViewResId, ApplicationListFragment.OnApplicationListFragmentInteraction onApplicationListFragmentInteraction) {
			super(itemView);
			mViewHolder = itemView;
			mActivity = activity;
			mApplicationIconImageView = (ImageView) itemView.findViewById(R.id.imageView_application_icon);
			mApplicationNameTextView = (TextView) itemView.findViewById(R.id.textView_application_name);
			mApplicationSelectionMode = applicationSelectionMode;
			mClickedViewResId = clickedViewResId;
			mOnApplicationListFragmentInteraction = onApplicationListFragmentInteraction;
		}

		public void setIconImage(Drawable icon) {
			mApplicationIconImageView.setImageDrawable(icon);
		}

		public void setNameText(String labelRes) {
			mApplicationNameTextView.setText(labelRes);
		}

		public void setPackageName(String packageName) {
			mPackageName = packageName;
		}

		public void configureOnClickListener() {
			mViewHolder.setOnClickListener(this);
		}

		@Override
		public void onClick(View v) {
			if (mApplicationSelectionMode == Constants.LAUNCH_APPLICATION_MODE) {
				mActivity.startActivity(mActivity.getPackageManager().getLaunchIntentForPackage(mPackageName));
				mActivity.finish();
			} else {
				mOnApplicationListFragmentInteraction.onClickApplicationIcon(mPackageName, mClickedViewResId);
				//((AppCompatActivity) mActivity).getSupportFragmentManager().popBackStack();
				((ApplicationListFragment) ((AppCompatActivity) mActivity).getSupportFragmentManager().findFragmentByTag("application_list_fragment"))
						.concealApplicationList();
			}
		}
	}
}
