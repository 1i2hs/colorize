package edu.skku.inho.colorize;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;

/**
 * Created by XEiN on 2/11/16.
 */
public class DrawableToBitmapConverter {
	public static Bitmap convertToBitmap(Drawable drawable, int widthPixels, int heightPixels) {
		Bitmap mutableBitmap = Bitmap.createBitmap(widthPixels, heightPixels, Bitmap.Config.RGB_565);
		Canvas canvas = new Canvas(mutableBitmap);
		drawable.setBounds(0, 0, widthPixels, heightPixels);
		drawable.draw(canvas);

		return mutableBitmap;
	}

	public static Bitmap convertToBitmap(Drawable drawable) {
		int drawableWidth = drawable.getIntrinsicWidth();
		int drawableHeight = drawable.getIntrinsicHeight();

		//Log.i("Drawable to Bitmap converter", "Width: " + drawableWidth + " Height: " + drawableHeight);

		Bitmap mutableBitmap = Bitmap.createBitmap(drawableWidth, drawableHeight, Bitmap.Config.ARGB_8888);
		Canvas canvas = new Canvas(mutableBitmap);
		drawable.setBounds(0, 0, drawableWidth, drawableHeight);
		drawable.draw(canvas);

		return mutableBitmap;
	}
}
