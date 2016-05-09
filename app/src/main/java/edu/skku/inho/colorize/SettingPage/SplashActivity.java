package edu.skku.inho.colorize.SettingPage;

import android.app.ActivityOptions;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.transition.ChangeImageTransform;
import android.util.Log;
import android.view.Window;
import android.widget.ImageView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import edu.skku.inho.colorize.ApplicationInfoBundle;
import edu.skku.inho.colorize.LockScreenDataManager;
import edu.skku.inho.colorize.R;

/**
 * Created by In-Ho Han on 2/11/16.
 */
public class SplashActivity extends AppCompatActivity {
	private static final String TAG = "SplashActivity";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		getWindow().requestFeature(Window.FEATURE_CONTENT_TRANSITIONS);

		super.onCreate(savedInstanceState);

		getWindow().setSharedElementEnterTransition(new ChangeImageTransform());

		setContentView(R.layout.activity_splash);

		final ImageView splashBrandIconImageView = (ImageView) findViewById(R.id.imageView_splash_brand_icon);

		final Handler splashHandler = new Handler();
		splashHandler.postDelayed(new Runnable() {
			@Override
			public void run() {
				makeApplicationList(readAppInfoFromDevice());

				Intent intent = new Intent(SplashActivity.this, SettingActivity.class);
				ActivityOptions options = ActivityOptions.makeSceneTransitionAnimation(SplashActivity.this, splashBrandIconImageView, "brandIcon");
				startActivity(intent, options.toBundle());

				splashHandler.postDelayed(new Runnable() {
					@Override
					public void run() {
						finish();
					}
				}, 1000);
			}
		}, 800);
	}

	private void makeApplicationList(List<ResolveInfo> applicationResolveInfoList) {
		ArrayList<ApplicationInfoBundle> applicationList = new ArrayList<>();

		for (ResolveInfo resolveInfo : applicationResolveInfoList) {
			ApplicationInfo temp = resolveInfo.activityInfo.applicationInfo;
			ApplicationInfoBundle applicationInfoBundle = new ApplicationInfoBundle();

			Drawable tempDrawable = temp.loadIcon(getPackageManager());
			applicationInfoBundle.setApplicationIcon(tempDrawable);
			applicationInfoBundle.setApplicationPackageName(temp.packageName);
			applicationInfoBundle.setApplicationName(temp.loadLabel(getPackageManager()).toString());
			applicationList.add(applicationInfoBundle);
		}


		LockScreenDataManager.getInstance(this).setApplicationList(sortApplicationInAlphabeticalOrder(applicationList));
		Log.d(TAG, ">>> end reading app info from device...");
	}

	protected List<ResolveInfo> readAppInfoFromDevice() {
		Log.d(TAG, ">>> start reading app info from device...");
		Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
		mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);
		return getPackageManager().queryIntentActivities(mainIntent, PackageManager.PERMISSION_GRANTED);
	}

	private ArrayList<ApplicationInfoBundle> sortApplicationInAlphabeticalOrder(ArrayList<ApplicationInfoBundle> applicationList) {
		Collections.sort(applicationList, new Comparator<ApplicationInfoBundle>() {
			@Override
			public int compare(ApplicationInfoBundle o1, ApplicationInfoBundle o2) {
				return o1.getApplicationName().compareToIgnoreCase(o2.getApplicationName());
			}
		});
		return applicationList;
	}
}
