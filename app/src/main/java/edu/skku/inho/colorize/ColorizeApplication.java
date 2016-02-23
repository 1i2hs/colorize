package edu.skku.inho.colorize;

import android.app.Application;

/**
 * Created by XEiN on 2/11/16.
 */
public class ColorizeApplication extends Application {
	@Override
	public void onCreate() {
		super.onCreate();
		ApplicationListProvider.initInstance();
	}
}
