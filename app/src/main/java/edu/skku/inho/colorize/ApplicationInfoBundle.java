package edu.skku.inho.colorize;

import android.content.Intent;
import android.graphics.drawable.Drawable;


/**
 * Created by XEiN on 1/28/16.
 */
public class ApplicationInfoBundle {
	private Drawable mApplicationIcon;
	private String mApplicationName;
	private Intent mIntentForPackage;


	public String getApplicationName() {
		return mApplicationName;
	}

	public void setApplicationName(String applicationName) {
		mApplicationName = applicationName;
	}

	public Intent getIntentForPackage() {
		return mIntentForPackage;
	}

	public void setIntentForPackage(Intent intentForPackage) {
		mIntentForPackage = intentForPackage;
	}

	/*public int getSize() {
		return mApplicationIcon.getAllocationByteCount();
	}*/

	// must be called after makeApplicationIcon() method
	public Drawable getApplicationIcon() {
		return mApplicationIcon;
	}

	public void setApplicationIcon(Drawable applicationIcon) {
		mApplicationIcon = applicationIcon;
	}
}
