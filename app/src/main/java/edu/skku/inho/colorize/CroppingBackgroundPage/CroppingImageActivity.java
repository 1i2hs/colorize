package edu.skku.inho.colorize.CroppingBackgroundPage;

import android.content.ContextWrapper;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.isseiaoki.simplecropview.CropImageView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import edu.skku.inho.colorize.Constants;
import edu.skku.inho.colorize.R;

public class CroppingImageActivity extends AppCompatActivity {
	private static final String TAG = "CroppingImageActivity";

	private Toolbar mToolbar;
	private CropImageView mCropImageView;
	private FloatingActionButton mCropImageButton;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_cropping_image);

		linkViewInstances();
		configureToolbar();
		configureCropImageView();
		configureCropImageButton();
		callGalleryApplication();
	}

	private void linkViewInstances() {
		mToolbar = (Toolbar) findViewById(R.id.toolbar);
		mCropImageView = (CropImageView) findViewById(R.id.cropImageView_cropped_image);
		mCropImageButton = (FloatingActionButton) findViewById(R.id.fab_crop_image);
	}

	private void configureToolbar() {
		setSupportActionBar(mToolbar);
		getSupportActionBar().setTitle(R.string.crop_image_to_use_as_background);
		mToolbar.setSubtitle(R.string.lock_screen_background_selection);
		mToolbar.setNavigationIcon(R.drawable.ic_arrow_back_white_24dp);
		mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				finish();
			}
		});
	}

	private void configureCropImageView() {
		mCropImageView.setMinFrameSizeInDp(200);
		mCropImageView.setInitialFrameScale(1.0F);
	}

	private void configureCropImageButton() {
		mCropImageButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				saveCroppedImageIntoInternalStorage();
				Toast.makeText(CroppingImageActivity.this, R.string.lock_screen_background_selected, Toast.LENGTH_SHORT).show();
				finish();
			}
		});
	}

	private void callGalleryApplication() {
		Intent intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
		//intent.addCategory(Intent.CATEGORY_OPENABLE);
		intent.setType("image/*");
		startActivityForResult(intent, Constants.LOCK_SCREEN_BACKGROUND_REQUEST);
	}

	private void saveCroppedImageIntoInternalStorage() {
		// Log.d(TAG, String.valueOf(bitmap));

		File backgroundImageFilePath = getDir(getResources().getString(R.string.background_image_file_dir_name), ContextWrapper.MODE_PRIVATE);
		File backgroundImageFile = new File(backgroundImageFilePath, getResources().getString(R.string.background_image_file_name));
		FileOutputStream out = null;

		Log.i(TAG, "Background image file updated: " + backgroundImageFile.delete());

		try {
			backgroundImageFile.createNewFile();
			out = new FileOutputStream(backgroundImageFile);

			mCropImageView.getCroppedBitmap().compress(Bitmap.CompressFormat.PNG, 100, out);

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				out.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		if (requestCode == Constants.LOCK_SCREEN_BACKGROUND_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {

			Uri uri = data.getData();

			try {
				Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);
				mCropImageView.setImageBitmap(bitmap);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}
