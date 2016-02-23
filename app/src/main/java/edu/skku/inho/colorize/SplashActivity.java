package edu.skku.inho.colorize;

import android.app.ActivityOptions;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.transition.ChangeImageTransform;
import android.view.Window;
import android.widget.ImageView;

public class SplashActivity extends AppCompatActivity {

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
		}, 2000);
	}
}
