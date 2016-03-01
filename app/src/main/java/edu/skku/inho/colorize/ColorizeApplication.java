package edu.skku.inho.colorize;

import android.app.Application;
import android.util.Log;

/**
 * Created by XEiN on 2/11/16.
 */
public class ColorizeApplication extends Application {
	@Override
	public void onCreate() {
		super.onCreate();
		Log.d("ColorizeApplication", "Application class called");
		LockScreenDataProvider.initInstance(this);
	}
}
