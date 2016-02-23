package edu.skku.inho.colorize;

import android.app.Activity;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by XEiN on 1/27/16.
 */
public class ApplicationListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
	private static final String TAG = "ApplicationListAdapter";

	private Activity mActivity;

	private ArrayList<ApplicationInfoBundle> mApplicationInfoBundleArrayList;

	public ApplicationListAdapter(ArrayList<ApplicationInfoBundle> applicationInfoBundleArrayList, Activity activity) {
		mApplicationInfoBundleArrayList = applicationInfoBundleArrayList;
		mActivity = activity;
	}

	@Override
	public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
		View applicationIconView = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_application_icon_name, parent, false);
		return new ApplicationIconViewHolder(applicationIconView, mActivity);
	}

	@Override
	public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, int position) {
		ApplicationInfoBundle temp = mApplicationInfoBundleArrayList.get(position);

		((ApplicationIconViewHolder) viewHolder).setIconImage(temp.getApplicationIcon());
		((ApplicationIconViewHolder) viewHolder).setNameText(temp.getApplicationName());
		((ApplicationIconViewHolder) viewHolder).setIntentForPackage(temp.getIntentForPackage());
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
		private Intent mIntentForPackage;


		public ApplicationIconViewHolder(View itemView, Activity activity) {
			super(itemView);
			mViewHolder = itemView;
			mActivity = activity;
			mApplicationIconImageView = (ImageView) itemView.findViewById(R.id.imageView_application_icon);
			mApplicationNameTextView = (TextView) itemView.findViewById(R.id.textView_application_name);
		}

		public void setIconImage(Drawable icon) {
			mApplicationIconImageView.setImageDrawable(icon);
		}

		public void setNameText(String labelRes) {
			mApplicationNameTextView.setText(labelRes);
		}

		public void setIntentForPackage(Intent intentForPackage) {
			mIntentForPackage = intentForPackage;
		}

		public void configureOnClickListener() {
			mViewHolder.setOnClickListener(this);
		}

		@Override
		public void onClick(View v) {
			mActivity.startActivity(mIntentForPackage);
			mActivity.finish();
		}
	}
}
