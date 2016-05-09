package edu.skku.inho.colorize;

import android.app.Application;
import android.util.Log;

/**
 * Created by In-Ho Han on 2/11/16.
 */
public class ColorizeApplication extends Application {
	@Override
	public void onCreate() {
		super.onCreate();
		Log.d("ColorizeApplication", "Application class called");
		LockScreenDataManager.initInstance(this);
	}
}
