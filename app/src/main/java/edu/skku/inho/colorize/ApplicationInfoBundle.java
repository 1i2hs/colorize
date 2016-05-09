package edu.skku.inho.colorize;

import android.graphics.drawable.Drawable;


/**
 * Created by In-Ho Han on 1/28/16.
 */
public class ApplicationInfoBundle {
	private Drawable mApplicationIcon;
	private String mApplicationName;
	private String mApplicationPackageName;


	public String getApplicationName() {
		return mApplicationName;
	}

	public void setApplicationName(String applicationName) {
		mApplicationName = applicationName;
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


	public String getApplicationPackageName() {
		return mApplicationPackageName;
	}

	public void setApplicationPackageName(String applicationPackageName) {
		mApplicationPackageName = applicationPackageName;
	}
}
