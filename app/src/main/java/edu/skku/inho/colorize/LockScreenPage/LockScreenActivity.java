package edu.skku.inho.colorize.LockScreenPage;

import android.content.BroadcastReceiver;
import android.content.ClipData;
import android.content.ClipDescription;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Canvas;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.DragEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;

import java.io.File;
import java.util.ArrayList;

import edu.skku.inho.colorize.ApplicationInfoBundle;
import edu.skku.inho.colorize.ApplicationListDialog.ApplicationListFragment;
import edu.skku.inho.colorize.Constants;
import edu.skku.inho.colorize.CroppingBackgroundPage.BackgroundImageFileChangedDateSignature;
import edu.skku.inho.colorize.IconGroupingModule.GroupColor;
import edu.skku.inho.colorize.Keys;
import edu.skku.inho.colorize.LockScreenDataProvider;
import edu.skku.inho.colorize.R;
import edu.skku.inho.colorize.RoundView;

public class LockScreenActivity extends AppCompatActivity implements ApplicationListFragment.OnApplicationListFragmentInteraction,
		View.OnClickListener {
	private final static String TAG = "LockScreenActivity";

	private ImageView mBackgroundImageView;

	private View mSelectionCircleView;

	private IconDragEventListener mIconDragEventListener;

	private ApplicationInfoBundle[] mApplicationShortcut = new ApplicationInfoBundle[4];

	private BroadcastReceiver mServiceStateReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			int messageFlag = intent.getIntExtra(Keys.UPDATE_SERVICE_MESSAGE, -1);
			if (messageFlag == Constants.COLOR_DATA_READY) {
				configureColorCircles();
				configureSelectionCircle();
				configureApplicationShortcuts();
			}

			if (messageFlag == Constants.UPDATE_SERVICE_DESTROYED) {
				finish();
			}
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.d(TAG, "onCreate Lock Screen");
		setContentView(R.layout.activity_lock_screen);

		getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED | WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);
		getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
			getWindow().setStatusBarColor(getColor(R.color.status_bar_color));
		} else {
			getWindow().setStatusBarColor(getResources().getColor(R.color.status_bar_color));
		}

		LocalBroadcastManager.getInstance(this).registerReceiver(mServiceStateReceiver, new IntentFilter(Keys.UPDATE_SERVICE_BROADCAST));

		configureBackground();
		// check whether the computed color data is ready
		//if (mSharedPreferences.getBoolean(Keys.IS_COLOR_DATA_READY, false) && mSharedPreferences.getBoolean(Keys.IS_LOCK_SCREEN_RUNNING, false)) {
		if (LockScreenDataProvider.getInstance(this).isColorDataReady() && LockScreenDataProvider.getInstance(this).isLockScreenRunning()) {
			configureColorCircles();
			configureSelectionCircle();
			configureApplicationShortcuts();
		} else {
			// this branch is passed when the UpdateService has been stopped abnormally
			Toast.makeText(this, R.string.update_service_not_running, Toast.LENGTH_LONG).show();
			finish();
		}
	}

	@Override
	protected void onStop() {
		super.onStop();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		LocalBroadcastManager.getInstance(this).unregisterReceiver(mServiceStateReceiver);
		Log.d(TAG, "LockScreenActivity Destroyed...");
	}

	private void configureBackground() {
		mBackgroundImageView = (ImageView) findViewById(R.id.imageView_background);

		File imageFile = new File(getDir(getResources().getString(R.string.background_image_file_dir_name), ContextWrapper.MODE_PRIVATE),
				getResources().getString(R.string.background_image_file_name));
		Glide.with(this).load(imageFile).signature(new BackgroundImageFileChangedDateSignature(imageFile.lastModified())).into(mBackgroundImageView);
	}

	/**
	 * get instances of color circle images(which categorizes applications into certain colors)
	 * from xml file, sets the color for each circle, and OnTouchListener to detect one of the
	 * color circle images is touched.
	 */
	protected void configureColorCircles() {
		ArrayList<GroupColor> groupColorPointList = LockScreenDataProvider.getInstance(this).getGroupColorList();
		for (int i = 0; i < 8; i++) {
			RoundView colorCircle;

			switch (i) {
				case 0:
					colorCircle = (RoundView) findViewById(R.id.view_first_color_circle);
					colorCircle.setTag(GroupColor.FIRST_COLOR);
					break;
				case 1:
					colorCircle = (RoundView) findViewById(R.id.view_second_color_circle);
					colorCircle.setTag(GroupColor.SECOND_COLOR);
					break;
				case 2:
					colorCircle = (RoundView) findViewById(R.id.view_third_color_circle);
					colorCircle.setTag(GroupColor.THIRD_COLOR);
					break;
				case 3:
					colorCircle = (RoundView) findViewById(R.id.view_fourth_color_circle);
					colorCircle.setTag(GroupColor.FOURTH_COLOR);
					break;
				case 4:
					colorCircle = (RoundView) findViewById(R.id.view_fifth_color_circle);
					colorCircle.setTag(GroupColor.FIFTH_COLOR);
					break;
				case 5:
					colorCircle = (RoundView) findViewById(R.id.view_sixth_color_circle);
					colorCircle.setTag(GroupColor.SIXTH_COLOR);
					break;
				case 6:
					colorCircle = (RoundView) findViewById(R.id.view_seventh_color_circle);
					colorCircle.setTag(GroupColor.SEVENTH_COLOR);
					break;
				case 7:
					colorCircle = (RoundView) findViewById(R.id.view_eighth_color_circle);
					colorCircle.setTag(GroupColor.EIGHTH_COLOR);
					break;
				default:
					Log.e(TAG, "number of color circle is overflown");
					colorCircle = null;
					break;
			}
			colorCircle.setColor(groupColorPointList.get(i).getARGBColor());
			colorCircle.setOnTouchListener(new OnColorTouchListener());
		}
	}

	/**
	 * get instance of circle image for selecting color from xml file and
	 * set OnDragListener to detect which color is dragged into the circle image
	 */
	protected void configureSelectionCircle() {
		mSelectionCircleView = findViewById(R.id.view_selection_circle);

		mIconDragEventListener = new IconDragEventListener();

		mSelectionCircleView.setOnDragListener(mIconDragEventListener);
		mSelectionCircleView.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				finish();
			}
		});
	}

	protected void configureApplicationShortcuts() {
		if (LockScreenDataProvider.getInstance(this).isUseApplicationShortcuts()) {
			int[] viewResId = {R.id.imageView_first_shortcut_app, R.id.imageView_second_shortcut_app, R.id.imageView_third_shortcut_app, R.id.imageView_fourth_shortcut_app};

			for (int i = 0; i < Constants.NUMBER_OF_APPLICATION_SHORTCUTS; i++) {
				// case : if there is an application shortcut set for i-th position
				if ((mApplicationShortcut[i] = LockScreenDataProvider.getInstance(this).getApplicationShortcut(i)) != null) {
					View shortcutApplicationView = findViewById(viewResId[i]);
					shortcutApplicationView.setVisibility(View.VISIBLE);
					shortcutApplicationView.setBackground(mApplicationShortcut[i].getApplicationIcon());
					shortcutApplicationView.setOnClickListener(this);
				}
			}
		}
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
		getSupportFragmentManager().popBackStack();
	}

	@Override
	protected void onPause() {
		super.onPause();
		getSupportFragmentManager().popBackStack();
		// code for blocking recent apps button click
		//ActivityManager activityManager = (ActivityManager) getApplicationContext().getSystemService(Context.ACTIVITY_SERVICE);
		//activityManager.moveTaskToFront(getTaskId(), 0);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
			case R.id.imageView_first_shortcut_app:
				startActivity(getPackageManager()
						.getLaunchIntentForPackage(mApplicationShortcut[Constants.FIRST_APPLICATION_SHORTCUT].getApplicationPackageName()));
				break;
			case R.id.imageView_second_shortcut_app:
				startActivity(getPackageManager()
						.getLaunchIntentForPackage(mApplicationShortcut[Constants.SECOND_APPLICATION_SHORTCUT].getApplicationPackageName()));
				break;
			case R.id.imageView_third_shortcut_app:
				startActivity(getPackageManager()
						.getLaunchIntentForPackage(mApplicationShortcut[Constants.THIRD_APPLICATION_SHORTCUT].getApplicationPackageName()));
				break;
			case R.id.imageView_fourth_shortcut_app:
				startActivity(getPackageManager()
						.getLaunchIntentForPackage(mApplicationShortcut[Constants.FOURTH_APPLICATION_SHORTCUT].getApplicationPackageName()));
				break;
		}
		finish();
	}

	@Override
	public void onClickApplicationIcon(String packageName, int clickedViewResId) {

	}

	private static class OnColorTouchListener implements View.OnTouchListener {
		@Override
		public boolean onTouch(View v, MotionEvent event) {
			Log.d(TAG, "onTouch");
			// Create a new ClipData.Item from the ImageView object's tag
			ClipData.Item item = new ClipData.Item((String) v.getTag());

			// Create a new ClipData using the tag as a label, the plain text MIME type, and
			// the already-created item. This will create a new ClipDescription object within the
			// ClipData, and set its MIME type entry to "text/plain"
			ClipData dragData = new ClipData((String) v.getTag(), new String[]{ClipDescription.MIMETYPE_TEXT_PLAIN}, item);

			// Instantiates the drag shadow builder.
			View.DragShadowBuilder myShadow = new ColorIconShadowBuilder(v);

			// Starts the drag

			v.startDrag(dragData,  // the data to be dragged
					myShadow,  // the drag shadow builder
					null,      // no need to use local data
					0          // flags (not currently used, set to 0)
			);

			return true;
		}
	}

	private static class ColorIconShadowBuilder extends View.DragShadowBuilder {

		// The drag shadow image, defined as a drawable thing
		private static Drawable shadow;

		// Defines the constructor for myDragShadowBuilder
		public ColorIconShadowBuilder(View roundColorView) {

			// Stores the View parameter passed to myDragShadowBuilder.
			super(roundColorView);

			// Creates a draggable image that will fill the Canvas provided by the system.
			GradientDrawable gradientDrawable = (GradientDrawable) roundColorView.getContext().getDrawable(R.drawable.shape_color_circle);
			gradientDrawable.setColor(((RoundView) roundColorView).getColor());
			shadow = gradientDrawable;
		}

		// Defines a callback that sends the drag shadow dimensions and touch point back to the
		// system.
		@Override
		public void onProvideShadowMetrics(Point size, Point touch) {
			// Defines local variables
			int width, height;

			// Sets the width of the shadow to half the width of the original View
			width = getView().getWidth();

			// Sets the height of the shadow to half the height of the original View
			height = getView().getHeight();

			// The drag shadow is a ColorDrawable. This sets its dimensions to be the same as the
			// Canvas that the system will provide. As a result, the drag shadow will fill the
			// Canvas.
			shadow.setBounds(0, 0, width, height);

			// Sets the size parameter's width and height values. These get back to the system
			// through the size parameter.
			size.set(width, height);

			// Sets the touch point's position to be in the middle of the drag shadow
			touch.set(width / 2, height / 2);
		}

		// Defines a callback that draws the drag shadow in a Canvas that the system constructs
		// from the dimensions passed in onProvideShadowMetrics().
		@Override
		public void onDrawShadow(Canvas canvas) {
			// Draws the ColorDrawable in the Canvas passed in from the system.
			shadow.draw(canvas);
		}
	}

	protected class IconDragEventListener implements View.OnDragListener {

		// This is the method that the system calls when it dispatches a drag event to the
		// listener.
		public boolean onDrag(View v, DragEvent event) {

			// Defines a variable to store the action type for the incoming event
			final int action = event.getAction();

			// Handles each of the expected events
			switch (action) {
				case DragEvent.ACTION_DRAG_STARTED:
					// Determines if this View can accept the dragged data
					if (event.getClipDescription().hasMimeType(ClipDescription.MIMETYPE_TEXT_PLAIN)) {

						// As an example of what your application might do,
						// applies a blue color tint to the View to indicate that it can accept
						// data.

						// Invalidate the view to force a redraw in the new tint
						v.invalidate();

						// returns true to indicate that the View can accept the dragged data.
						return true;

					}

					// Returns false. During the current drag and drop operation, this View will
					// not receive events again until ACTION_DRAG_ENDED is sent.
					return false;

				case DragEvent.ACTION_DRAG_ENTERED:
					// Applies a green tint to the View. Return true; the return value is ignored.


					// Invalidate the view to force a redraw in the new tint
					v.invalidate();

					return true;

				case DragEvent.ACTION_DRAG_LOCATION:

					// Ignore the event
					return true;

				case DragEvent.ACTION_DRAG_EXITED:
					// Re-sets the color tint to blue. Returns true; the return value is ignored.

					// Invalidate the view to force a redraw in the new tint
					v.invalidate();

					return true;

				case DragEvent.ACTION_DROP:
					// Gets the item containing the dragged data
					ClipData.Item item = event.getClipData().getItemAt(0);

					// Gets the text data from the item.
					String selectedColor = item.getText().toString();

					// Invalidates the view to force a redraw
					v.invalidate();

					ApplicationListFragment applicationListFragment = ApplicationListFragment
							.newInstance(selectedColor, Constants.LAUNCH_APPLICATION_MODE, event.getX() + v.getLeft(), event.getY() + v.getTop());
					FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
					fragmentTransaction.add(android.R.id.content, applicationListFragment, "application_list_fragment");
					fragmentTransaction.addToBackStack(null);
					fragmentTransaction.commit();

					// Returns true. DragEvent.getResult() will return true.
					return true;

				case DragEvent.ACTION_DRAG_ENDED:
					// Turns off any color tinting

					// Invalidates the view to force a redraw
					v.invalidate();

					/*if (event.getResult()) {
						Toast.makeText(v.getContext(), "The drop was handled.", Toast.LENGTH_LONG).show();
					} else {
						Toast.makeText(v.getContext(), "The drop didn't work.", Toast.LENGTH_LONG).show();
					}*/

					// returns true; the value is ignored.
					return true;

				// An unknown action type was received.
				default:
					Log.e("DragDrop Example", "Unknown action type received by OnDragListener.");
					break;
			}

			return false;
		}
	}


}
