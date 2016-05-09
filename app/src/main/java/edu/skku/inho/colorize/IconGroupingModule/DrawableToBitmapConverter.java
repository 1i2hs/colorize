package edu.skku.inho.colorize.IconGroupingModule;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;

/**
 * Created by In-Ho Han on 2/11/16.
 *
 * class that converts drawable class object into bitmap class.
 */
public class DrawableToBitmapConverter {
	/**
	 * converts drawable class object into bitmap class with designated size
	 *
	 * @param drawable     drawable class object to be converted into bitmap class
	 * @param widthPixels  width after conversion in pixel
	 * @param heightPixels height after conversion in pixel
	 * @return bitmap class object
	 */
	public static Bitmap convertToBitmap(Drawable drawable, int widthPixels, int heightPixels) {
		Bitmap mutableBitmap = Bitmap.createBitmap(widthPixels, heightPixels, Bitmap.Config.ARGB_8888);
		Canvas canvas = new Canvas(mutableBitmap);
		drawable.setBounds(0, 0, widthPixels, heightPixels);
		drawable.draw(canvas);

		return mutableBitmap;
	}

	/**
	 * converts drawable class object into bitmap class
	 * @param drawable  drawable class object to be converted into bitmap class
	 * @return bitmap class object
	 */
	public static Bitmap convertToBitmap(Drawable drawable) {
		int drawableWidth = drawable.getIntrinsicWidth();
		int drawableHeight = drawable.getIntrinsicHeight();

		Bitmap mutableBitmap = Bitmap.createBitmap(drawableWidth, drawableHeight, Bitmap.Config.ARGB_8888);
		Canvas canvas = new Canvas(mutableBitmap);
		drawable.setBounds(0, 0, drawableWidth, drawableHeight);
		drawable.draw(canvas);

		return mutableBitmap;
	}
}
