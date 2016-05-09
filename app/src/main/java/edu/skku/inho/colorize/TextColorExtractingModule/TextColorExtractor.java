package edu.skku.inho.colorize.TextColorExtractingModule;

import android.graphics.Bitmap;
import android.support.v7.graphics.Palette;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by In-Ho Han on 5/4/16.
 */
public class TextColorExtractor {
	private static final String TAG = "TextColorExtractor";
	private ArrayList<Integer> mColorList;

	private TextColorExtractor(ArrayList<Integer> colorList) {
		mColorList = colorList;
	}

	public static Builder from(Bitmap bitmapImage) {
		return new Builder(bitmapImage);
	}

	public ArrayList<Integer> getColorList() {
		return mColorList;
	}

	public static class Builder {
		private Bitmap mBitmapImage;

		public Builder(Bitmap bitmapImage) {
			mBitmapImage = bitmapImage;
		}

		public TextColorExtractor extractTextColors() {
			List<Palette.Swatch> swatches = Palette.from(mBitmapImage).generate().getSwatches();
			return new TextColorExtractor(getTitleTextColors(swatches));
		}

		private ArrayList<Integer> getTitleTextColors(List<Palette.Swatch> swatches) {
			ArrayList<Integer> colorList = new ArrayList<>();
			for (Palette.Swatch swatch : swatches) {
				colorList.add(swatch.getTitleTextColor());
				colorList.add(swatch.getBodyTextColor());
			}
			return colorList;
		}
	}
}
