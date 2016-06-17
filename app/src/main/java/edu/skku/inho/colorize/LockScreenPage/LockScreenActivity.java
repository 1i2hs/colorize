package edu.skku.inho.colorize.LockScreenPage;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.BroadcastReceiver;
import android.content.Context;
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
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.DragEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import java.util.ArrayList;

import edu.skku.inho.colorize.ApplicationInfoBundle;
import edu.skku.inho.colorize.ApplicationListDialog.ApplicationListFragment;
import edu.skku.inho.colorize.Constants;
import edu.skku.inho.colorize.CustomView.DigitalClockView;
import edu.skku.inho.colorize.IconGroupingModule.GroupColor;
import edu.skku.inho.colorize.Keys;
import edu.skku.inho.colorize.LockScreenDataManager;
import edu.skku.inho.colorize.R;

/**
 * Created by In-Ho Han on 2/11/16.
 */
public class LockScreenActivity extends AppCompatActivity implements ApplicationListFragment.OnApplicationListFragmentInteraction,
		View.OnClickListener,
		View.OnTouchListener,
		View.OnDragListener {
	private final static String TAG = "LockScreenActivity";

	private ImageView mBackgroundImageView;

	private FrameLayout mBackgroundShadeFrameLayout;

	private DigitalClockView mDigitalClockTimeView;
	private DigitalClockView mDigitalClockDateView;

	private View mSelectionView;

	private ApplicationInfoBundle[] mApplicationShortcut = new ApplicationInfoBundle[4];

	//private float mTargetStraightTransitionValue;
	//private float mTargetDiagonalTransitionValue;
	//private float mLastStraightTransitionValue = 0.0F;
	//private float mLastDiagonalTransitionValue = 0.0F;

	private boolean mIsSelectionViewBackgroundChanged = false;

	private RelativeLayout mColorPaletteRelativeLayout;
	private DisplayMetrics mDisplayMetrics;

	private boolean mCircularRevealAnimationStarted = false;

	// receiver that is executed when ColorGroupingService re-computes the group colors due to
	// changes of application packages such as update, deletion, installation, etc. This receiver
	// is executed only when the service recomputes the group color while this activity is running.
	private BroadcastReceiver mServiceStateReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			int messageFlag = intent.getIntExtra(Keys.COLOR_GROUPING_SERVICE_MESSAGE, -1);
			if (messageFlag == Constants.COLOR_DATA_READY) {
				//configureColorCircles();
				configureColorPalette();

				configureSelectionCircle();
				configureApplicationShortcuts();
			}

			if (messageFlag == Constants.COLOR_GROUPING_SERVICE_DESTROYED) {
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

		LocalBroadcastManager.getInstance(this).registerReceiver(mServiceStateReceiver, new IntentFilter(Keys.COLOR_GROUPING_SERVICE_BROADCAST));

		configureBackground();
		// check whether the computed color data is ready
		if (LockScreenDataManager.getInstance(this).isColorDataReady() && LockScreenDataManager.getInstance(this).isLockScreenRunning()) {
			configureDisplayMetrics();
			configureBackgroundShade();
			configureDigitalClock();
			configureSelectionCircle();
			//configureColorCircles();
			//configureUnlockCircle();

			configureColorPalette();
			configureUnlockTile();

			configureApplicationShortcuts();
		} else {
			// this branch is passed when the ColorGroupingService has been stopped abnormally
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

	protected void configureBackground() {
		mBackgroundImageView = (ImageView) findViewById(R.id.imageView_background);
		LockScreenDataManager.getInstance(this).applyBackgroundImage(mBackgroundImageView);
	}

	private void configureDisplayMetrics() {
		mDisplayMetrics = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(mDisplayMetrics);

		//mTargetStraightTransitionValue = getResources().getDimensionPixelSize(R.dimen.color_circle_straight_transition_value);
		//mTargetDiagonalTransitionValue = getResources().getDimensionPixelSize(R.dimen.color_circle_diagonal_transition_value);
	}

	protected void configureBackgroundShade() {
		mBackgroundShadeFrameLayout = (FrameLayout) findViewById(R.id.frameLayout_background_shade);
		mBackgroundShadeFrameLayout.setAlpha(0.0F);
	}

	protected void configureDigitalClock() {
		mDigitalClockTimeView = (DigitalClockView) findViewById(R.id.digitalClockView_time);
		mDigitalClockDateView = (DigitalClockView) findViewById(R.id.digitalClockView_date);

		int textColor = LockScreenDataManager.getInstance(this).getDigitalClockTextColor();
		mDigitalClockTimeView.setTextColor(textColor);
		mDigitalClockDateView.setTextColor(textColor);
	}

	protected void configureSelectionCircle() {
		mSelectionView = findViewById(R.id.view_selection_circle);
		mSelectionView.setOnTouchListener(this);
	}

	protected void configureColorPalette() {
		mColorPaletteRelativeLayout = (RelativeLayout) findViewById(R.id.relativeLayout_color_palette);
		ArrayList<GroupColor> groupColorPointList = LockScreenDataManager.getInstance(this).getGroupColorList();
		for (int i = 0; i < 7; i++) {
			FrameLayout colorTileFrameLayout;

			switch (i) {
				case 0:
					colorTileFrameLayout = (FrameLayout) findViewById(R.id.frameLayout_first_color);
					colorTileFrameLayout.setTag(GroupColor.FIRST_COLOR);
					break;
				case 1:
					colorTileFrameLayout = (FrameLayout) findViewById(R.id.frameLayout_second_color);
					colorTileFrameLayout.setTag(GroupColor.SECOND_COLOR);
					break;
				case 2:
					colorTileFrameLayout = (FrameLayout) findViewById(R.id.frameLayout_third_color);
					colorTileFrameLayout.setTag(GroupColor.THIRD_COLOR);
					break;
				case 3:
					colorTileFrameLayout = (FrameLayout) findViewById(R.id.frameLayout_fourth_color);
					colorTileFrameLayout.setTag(GroupColor.FOURTH_COLOR);
					break;
				case 4:
					colorTileFrameLayout = (FrameLayout) findViewById(R.id.frameLayout_fifth_color);
					colorTileFrameLayout.setTag(GroupColor.FIFTH_COLOR);
					break;
				case 5:
					colorTileFrameLayout = (FrameLayout) findViewById(R.id.frameLayout_sixth_color);
					colorTileFrameLayout.setTag(GroupColor.SIXTH_COLOR);
					break;
				case 6:
					colorTileFrameLayout = (FrameLayout) findViewById(R.id.frameLayout_seventh_color);
					colorTileFrameLayout.setTag(GroupColor.SEVENTH_COLOR);
					break;
				default:
					Log.e(TAG, "number of color circle is overflown");
					colorTileFrameLayout = null;
					break;
			}
			colorTileFrameLayout.setBackgroundColor(groupColorPointList.get(i).getARGB());
			colorTileFrameLayout.setOnDragListener(this);
		}

		mColorPaletteRelativeLayout.setVisibility(View.INVISIBLE);
	}

	protected void configureUnlockTile() {
		FrameLayout unlockTileFrameLayout = (FrameLayout) findViewById(R.id.frameLayout_unlock);
		unlockTileFrameLayout.setTag(Constants.UNLOCK);
		unlockTileFrameLayout.setOnDragListener(this);
	}

	/*protected void configureColorCircles() {
		ArrayList<GroupColor> groupColorPointList = LockScreenDataManager.getInstance(this).getGroupColorList();
		for (int i = 0; i < 7; i++) {
			CircleView colorCircleView;

			switch (i) {
				case 0:
					colorCircleView = (CircleView) findViewById(R.id.view_first_color_circle);
					colorCircleView.setTag(GroupColor.FIRST_COLOR);
					break;
				case 1:
					colorCircleView = (CircleView) findViewById(R.id.view_second_color_circle);
					colorCircleView.setTag(GroupColor.SECOND_COLOR);
					break;
				case 2:
					colorCircleView = (CircleView) findViewById(R.id.view_third_color_circle);
					colorCircleView.setTag(GroupColor.THIRD_COLOR);
					break;
				case 3:
					colorCircleView = (CircleView) findViewById(R.id.view_fourth_color_circle);
					colorCircleView.setTag(GroupColor.FOURTH_COLOR);
					break;
				case 4:
					colorCircleView = (CircleView) findViewById(R.id.view_fifth_color_circle);
					colorCircleView.setTag(GroupColor.FIFTH_COLOR);
					break;
				case 5:
					colorCircleView = (CircleView) findViewById(R.id.view_sixth_color_circle);
					colorCircleView.setTag(GroupColor.SIXTH_COLOR);
					break;
				case 6:
					colorCircleView = (CircleView) findViewById(R.id.view_seventh_color_circle);
					colorCircleView.setTag(GroupColor.SEVENTH_COLOR);
					break;
				default:
					Log.e(TAG, "number of color circle is overflown");
					colorCircleView = null;
					break;
			}
			colorCircleView.setColor(groupColorPointList.get(i).getARGB());
			colorCircleView.setOnDragListener(this);
		}
	}*/

	/*protected void configureUnlockCircle() {
		CircleView unlockCircle = (CircleView) findViewById(R.id.view_unlock_circle);
		unlockCircle.setTag(Constants.UNLOCK);
		unlockCircle.setOnDragListener(this);
	}*/

	protected void configureApplicationShortcuts() {
		if (LockScreenDataManager.getInstance(this).isUseApplicationShortcuts()) {
			int[] viewResId = {R.id.imageView_first_shortcut_app, R.id.imageView_second_shortcut_app, R.id.imageView_third_shortcut_app, R.id.imageView_fourth_shortcut_app};

			for (int i = 0; i < Constants.NUMBER_OF_APPLICATION_SHORTCUTS; i++) {
				// case : if there is an application shortcut set for i-th position
				if ((mApplicationShortcut[i] = LockScreenDataManager.getInstance(this).getApplicationShortcut(i)) != null) {
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
				((ApplicationListFragment) fragment).concealApplicationList();
				return;
			}
		}
		getSupportFragmentManager().popBackStack();
	}

	@Override
	protected void onPause() {
		super.onPause();
		getSupportFragmentManager().popBackStack();
	}

	@Override
	public void onClickApplicationIcon(String packageName, int clickedViewResId) {

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
	public boolean onTouch(View v, MotionEvent event) {
		// Instantiates the drag shadow builder.
		if (event.getAction() == MotionEvent.ACTION_DOWN && !mCircularRevealAnimationStarted) {
			View.DragShadowBuilder myShadow = new SelectionCircleShadowBuilder(v);


			int size = mSelectionView.getHeight() / 2;
			revealColorPalette(mSelectionView.getX() + size, mSelectionView.getY() + size);
			// Starts the drag
			v.startDrag(null,  // the data to be dragged
					myShadow,  // the drag shadow builder
					null,      // no need to use local data
					0          // flags (not currently used, set to 0)
			);
			return true;
		} else {
			return false;
		}
	}

	private void revealColorPalette(float clickedViewX, float clickedViewY) {
		Animator anim = createCircularRevealAnimator(clickedViewX, clickedViewY, 0, calculateRadius(clickedViewX, clickedViewY));
		mColorPaletteRelativeLayout.setVisibility(View.VISIBLE);
		anim.addListener(new AnimatorListenerAdapter() {
			@Override
			public void onAnimationEnd(Animator animation) {
				Log.d(TAG, "revealAnimationEnd");
				super.onAnimationEnd(animation);
				mCircularRevealAnimationStarted = false;
			}

			@Override
			public void onAnimationStart(Animator animation) {
				Log.d(TAG, "revealAnimationStart");
				super.onAnimationStart(animation);
				mCircularRevealAnimationStarted = true;
			}


		});
		anim.setDuration(500);
		anim.start();

	}

	private Animator createCircularRevealAnimator(float clickedViewX, float clickedViewY, float initialRadius, float finalRadius) {
		Animator anim = ViewAnimationUtils
				.createCircularReveal(mColorPaletteRelativeLayout, (int) clickedViewX, (int) clickedViewY, initialRadius, finalRadius);
		return anim;
	}

	private float calculateRadius(float clickedViewX, float clickedViewY) {
		double diagonal1 = Math.sqrt(Math.pow(clickedViewX, 2) + Math.pow(clickedViewY, 2));
		double diagonal2 = Math.sqrt(Math.pow(clickedViewX, 2) + Math.pow(mDisplayMetrics.heightPixels - clickedViewY, 2));
		return (float) Math.max(diagonal1, diagonal2);
	}

	public boolean onDrag(View v, DragEvent event) {
		final int action = event.getAction();

		switch (action) {
			case DragEvent.ACTION_DRAG_STARTED:
				if (!mIsSelectionViewBackgroundChanged) {
					AnimatorSet animatorSet = new AnimatorSet();
					ObjectAnimator alphaAnimator1 = ObjectAnimator.ofFloat(mBackgroundShadeFrameLayout, "alpha", 0.0F, 1.0F);
					ObjectAnimator alphaAnimator2 = ObjectAnimator.ofFloat(mSelectionView, "alpha", 1.0F, 0.0F);
					animatorSet.playTogether(alphaAnimator1, alphaAnimator2);
					animatorSet.start();
					mIsSelectionViewBackgroundChanged = true;
				}
				// Returns false. During the current drag and drop operation, this View will
				// not receive events again until ACTION_DRAG_ENDED is sent.
				//toggleColorCircles(v, true);
				return true;
			case DragEvent.ACTION_DRAG_ENTERED:
				// Invalidate the view to force a redraw in the new tint
				v.invalidate();
				return true;
			case DragEvent.ACTION_DRAG_LOCATION:
				// Ignore the event
				return true;
			case DragEvent.ACTION_DRAG_EXITED:
				// Invalidate the view to force a redraw in the new tint
				v.invalidate();

				return true;
			case DragEvent.ACTION_DROP:
				//if (mLastStraightTransitionValue == mTargetStraightTransitionValue) {
				String selectedColor = (String) v.getTag();

				// Invalidates the view to force a redraw
				v.invalidate();

				if (selectedColor.equals(Constants.UNLOCK)) {
					finish();
				} else {
					ApplicationListFragment applicationListFragment = ApplicationListFragment
							.newInstance(selectedColor, Constants.LAUNCH_APPLICATION_MODE, event.getX() + v.getLeft(), event.getY() + v.getTop());
					FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
					fragmentTransaction.add(android.R.id.content, applicationListFragment, "application_list_fragment");
					fragmentTransaction.addToBackStack(null);
					fragmentTransaction.commit();
				}
				// Returns true. DragEvent.getResult() will return true.
				return true;
			//}
			//return false;
			case DragEvent.ACTION_DRAG_ENDED:
				if (mIsSelectionViewBackgroundChanged) {
					AnimatorSet animatorSet = new AnimatorSet();
					ObjectAnimator alphaAnimator1 = ObjectAnimator.ofFloat(mBackgroundShadeFrameLayout, "alpha", 1.0F, 0.0F);
					ObjectAnimator alphaAnimator2 = ObjectAnimator.ofFloat(mSelectionView, "alpha", 0.0F, 1.0F);
					animatorSet.playTogether(alphaAnimator1, alphaAnimator2);
					animatorSet.start();
					mIsSelectionViewBackgroundChanged = false;

					int size = mSelectionView.getHeight() / 2;
					concealColorPalette(mSelectionView.getX() + size, mSelectionView.getY() + size);
				}
				//toggleColorCircles(v, false);
				// Invalidates the view to force a redraw
				v.invalidate();
				return true;
			// An unknown action type was received.
			default:
				Log.e(TAG, "Unknown action type received by OnDragListener.");
				break;
		}

		return false;
	}

	private void concealColorPalette(float clickedViewX, float clickedViewY) {
		Animator anim = createCircularRevealAnimator(clickedViewX, clickedViewY, calculateRadius(clickedViewX, clickedViewY), 0);
		anim.addListener(new AnimatorListenerAdapter() {
			@Override
			public void onAnimationEnd(Animator animation) {
				Log.d(TAG, "concealAnimationEnd");
				super.onAnimationEnd(animation);
				if (!mCircularRevealAnimationStarted) {
					mColorPaletteRelativeLayout.setVisibility(View.INVISIBLE);
				}
			}
		});
		anim.setDuration(500);
		anim.start();
	}

	/*private void toggleColorCircles(View colorCircleView, boolean spread) {
		if (colorCircleView.getTag() != null) {
			float initStraightTransitionValue = 0.0F;
			float targetStraightTransitionValue = 0.0F;
			float initDiagonalTransitionValue = 0.0F;
			float targetDiagonalTransitionValue = 0.0F;

			AnimatorSet spreadAnimatorSet = new AnimatorSet();
			ObjectAnimator translationXObjectAnimator;
			ObjectAnimator translationYObjectAnimator;

			if (spread) {
				spreadAnimatorSet.setInterpolator(new AccelerateInterpolator());
				spreadAnimatorSet.setDuration(200);
				targetStraightTransitionValue = mTargetStraightTransitionValue;
				targetDiagonalTransitionValue = mTargetDiagonalTransitionValue;
			} else {
				spreadAnimatorSet.setInterpolator(new AccelerateInterpolator());
				spreadAnimatorSet.setDuration(300);
				initStraightTransitionValue = mLastStraightTransitionValue;
				initDiagonalTransitionValue = mLastDiagonalTransitionValue;
			}

			String tag = (String) colorCircleView.getTag();
			switch (tag) {
				case GroupColor.FIRST_COLOR:
					translationYObjectAnimator = ObjectAnimator
							.ofFloat(colorCircleView, "translationY", -initStraightTransitionValue, -targetStraightTransitionValue);
					spreadAnimatorSet.play(translationYObjectAnimator);
					break;
				case GroupColor.SECOND_COLOR:
					translationXObjectAnimator = ObjectAnimator
							.ofFloat(colorCircleView, "translationX", initDiagonalTransitionValue, targetDiagonalTransitionValue);
					translationYObjectAnimator = ObjectAnimator
							.ofFloat(colorCircleView, "translationY", -initDiagonalTransitionValue, -targetDiagonalTransitionValue);
					translationXObjectAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
						@Override
						public void onAnimationUpdate(ValueAnimator animation) {
							mLastDiagonalTransitionValue = (float) animation.getAnimatedValue();
						}
					});
					spreadAnimatorSet.playTogether(translationXObjectAnimator, translationYObjectAnimator);
					break;
				case GroupColor.THIRD_COLOR:
					translationXObjectAnimator = ObjectAnimator
							.ofFloat(colorCircleView, "translationX", initStraightTransitionValue, targetStraightTransitionValue);
					translationXObjectAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
						@Override
						public void onAnimationUpdate(ValueAnimator animation) {
							mLastStraightTransitionValue = (float) animation.getAnimatedValue();
						}
					});
					spreadAnimatorSet.play(translationXObjectAnimator);
					break;
				case GroupColor.FOURTH_COLOR:
					translationXObjectAnimator = ObjectAnimator
							.ofFloat(colorCircleView, "translationX", initDiagonalTransitionValue, targetDiagonalTransitionValue);
					translationYObjectAnimator = ObjectAnimator
							.ofFloat(colorCircleView, "translationY", initDiagonalTransitionValue, targetDiagonalTransitionValue);
					spreadAnimatorSet.playTogether(translationXObjectAnimator, translationYObjectAnimator);
					break;
				case GroupColor.FIFTH_COLOR:
					translationXObjectAnimator = ObjectAnimator
							.ofFloat(colorCircleView, "translationX", -initDiagonalTransitionValue, -targetDiagonalTransitionValue);
					translationYObjectAnimator = ObjectAnimator
							.ofFloat(colorCircleView, "translationY", initDiagonalTransitionValue, targetDiagonalTransitionValue);
					spreadAnimatorSet.playTogether(translationXObjectAnimator, translationYObjectAnimator);
					break;
				case GroupColor.SIXTH_COLOR:
					translationXObjectAnimator = ObjectAnimator
							.ofFloat(colorCircleView, "translationX", -initStraightTransitionValue, -targetStraightTransitionValue);
					spreadAnimatorSet.play(translationXObjectAnimator);
					break;
				case GroupColor.SEVENTH_COLOR:
					translationXObjectAnimator = ObjectAnimator
							.ofFloat(colorCircleView, "translationX", -initDiagonalTransitionValue, -targetDiagonalTransitionValue);
					translationYObjectAnimator = ObjectAnimator
							.ofFloat(colorCircleView, "translationY", -initDiagonalTransitionValue, -targetDiagonalTransitionValue);
					spreadAnimatorSet.playTogether(translationXObjectAnimator, translationYObjectAnimator);
					break;
				case Constants.UNLOCK:
					translationYObjectAnimator = ObjectAnimator
							.ofFloat(colorCircleView, "translationY", initStraightTransitionValue, targetStraightTransitionValue);
					spreadAnimatorSet.play(translationYObjectAnimator);
					break;
			}
			spreadAnimatorSet.start();
		}
	}*/

	private static class SelectionCircleShadowBuilder extends View.DragShadowBuilder {

		// The drag shadow image, defined as a drawable thing
		private static Drawable shadow;

		// Defines the constructor for myDragShadowBuilder
		public SelectionCircleShadowBuilder(View selectionCircleView) {

			// Stores the View parameter passed to myDragShadowBuilder.
			super(selectionCircleView);

			// Creates a draggable image that will fill the Canvas provided by the system.
			GradientDrawable gradientDrawable = (GradientDrawable) selectionCircleView.getContext().getDrawable(R.drawable.shape_select_circle);
			//gradientDrawable.setColor(((RoundView) selectionCircleView).getColor());

			shadow = gradientDrawable;
		}

		// Defines a callback that sends the drag shadow dimensions and touch point back to the
		// system.
		@Override
		public void onProvideShadowMetrics(Point size, Point touch) {
			// Defines local variables
			int width, height;

			// Sets the width of the shadow to half the width of the original View
			width = (int) (getView().getWidth() * 0.8F);

			// Sets the height of the shadow to half the height of the original View
			height = (int) (getView().getHeight() * 0.8F);

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

}
