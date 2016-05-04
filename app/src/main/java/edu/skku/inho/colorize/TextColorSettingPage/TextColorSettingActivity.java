package edu.skku.inho.colorize.TextColorSettingPage;

import android.content.ContextWrapper;
import android.graphics.BitmapFactory;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.bumptech.glide.Glide;

import java.io.File;
import java.util.ArrayList;

import edu.skku.inho.colorize.CroppingBackgroundPage.BackgroundImageFileChangedDateSignature;
import edu.skku.inho.colorize.CustomView.DigitalClockView;
import edu.skku.inho.colorize.LockScreenDataProvider;
import edu.skku.inho.colorize.R;
import edu.skku.inho.colorize.TextColorExtractingModule.TextColorExtractor;

/**
 * Created by XEiN on 5/4/16.
 */
public class TextColorSettingActivity extends AppCompatActivity {
	private final static String TAG = "TextColorSettingActivity";

	private Toolbar mToolbar;
	private ImageView mBackgroundImageView;
	private DigitalClockView mDigitalClockTimeView;
	private DigitalClockView mDigitalClockDateView;
	private LinearLayout mRecommendedColorListLinearLayout;

	private int mSelectedColor = -1;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_text_color_setting);

		File imageFile = new File(getDir(getResources().getString(R.string.background_image_file_dir_name), ContextWrapper.MODE_PRIVATE),
				getResources().getString(R.string.background_image_file_name));

		linkViewInstances();
		configureToolbar();
		configureBackground(imageFile);
		configureDigitalClock();
		configureRecommendedColorList(imageFile);
	}

	@Override
	protected void onStop() {
		super.onStop();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
	}

	private void linkViewInstances() {
		mToolbar = (Toolbar) findViewById(R.id.toolbar);
		mBackgroundImageView = (ImageView) findViewById(R.id.imageView_background);
		mDigitalClockTimeView = (DigitalClockView) findViewById(R.id.digitalClockView_time);
		mDigitalClockDateView = (DigitalClockView) findViewById(R.id.digitalClockView_date);
		mRecommendedColorListLinearLayout = (LinearLayout) findViewById(R.id.linearLayout_recommended_color_list);
	}

	private void configureToolbar() {
		setSupportActionBar(mToolbar);
		getSupportActionBar().setTitle(R.string.lock_screen_digital_clock_text_color_selection);
		mToolbar.setSubtitle(R.string.select_from_text_color_recommendation);
		mToolbar.setNavigationIcon(R.drawable.ic_arrow_back_white_24dp);
		mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				LockScreenDataProvider.getInstance(TextColorSettingActivity.this).setDigitalClockTextColor(mSelectedColor);
				finish();
			}
		});
	}

	private void configureBackground(File imageFile) {
		Glide.with(this).load(imageFile).signature(new BackgroundImageFileChangedDateSignature(imageFile.lastModified())).into(mBackgroundImageView);
	}

	private void configureDigitalClock() {
		int color = LockScreenDataProvider.getInstance(this).getDigitalClockTextColor();
		mDigitalClockTimeView.setTextColor(color);
		mDigitalClockDateView.setTextColor(color);
	}

	private void configureRecommendedColorList(File imageFile) {
		ArrayList<Integer> colorList = TextColorExtractor.from(BitmapFactory.decodeFile(imageFile.getPath())).extractTextColors().getColorList();

		DisplayMetrics displayMetrics = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
		int size = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 40, displayMetrics);
		int margin = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 4, displayMetrics);

		LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(size, size);
		layoutParams.setMargins(margin, 0, margin, 0);

		// make ImageView for default clock's font color and add it to the LinearLayout;
		mRecommendedColorListLinearLayout
				.addView(configureRecommendedColorImageView(getResources().getColor(R.color.colorTernaryText), layoutParams));
		mRecommendedColorListLinearLayout
				.addView(configureRecommendedColorImageView(getResources().getColor(R.color.colorPrimaryText), layoutParams));

		for (int i = 0; i < colorList.size(); i++) {
			// make ImageView for recommended clock's font color and add it to the LinearLayout;
			final int color = colorList.get(i);
			mRecommendedColorListLinearLayout.addView(configureRecommendedColorImageView(color, layoutParams));
		}
	}

	private ImageView configureRecommendedColorImageView(final int color, LinearLayout.LayoutParams layoutParams) {
		ImageView colorImageView = new ImageView(this);
		colorImageView.setLayoutParams(layoutParams);
		colorImageView.setBackground(getResources().getDrawable(R.drawable.shape_color_circle));
		((GradientDrawable) colorImageView.getBackground()).setColor(color);
		colorImageView.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				mDigitalClockTimeView.setTextColor(color);
				mDigitalClockDateView.setTextColor(color);
				mSelectedColor = color;
			}
		});

		return colorImageView;
	}

	@Override
	public void onBackPressed() {
		LockScreenDataProvider.getInstance(this).setDigitalClockTextColor(mSelectedColor);
		super.onBackPressed();
	}

	@Override
	protected void onPause() {
		super.onPause();
	}
}
