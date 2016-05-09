package edu.skku.inho.colorize.IconGroupingModule;

import java.util.ArrayList;

import edu.skku.inho.colorize.ApplicationInfoBundle;

/**
 * Created by In-Ho Han on 2/11/16.
 *
 * class that represents one of 7 standard colors.
 * the color is based on CIE-L*a*b* color space.
 * extends Color class.
 */
public class GroupColor extends Color {
	/**
	 * constants for 7 standard colors
	 */
	public static final String FIRST_COLOR = "first_color";
	public static final String SECOND_COLOR = "second_color";
	public static final String THIRD_COLOR = "third_color";
	public static final String FOURTH_COLOR = "fourth_color";
	public static final String FIFTH_COLOR = "fifth_color";
	public static final String SIXTH_COLOR = "sixth_color";
	public static final String SEVENTH_COLOR = "seventh_color";

	private int mARGB; // ARGB value for this color
	private ArrayList<ApplicationInfoBundle> mApplicationList;  // list of application info that this standard color has

	/**
	 * Constructor
	 *
	 * @param l L* value for the color
	 * @param a a* value for the color
	 * @param b b* value for the color
	 */
	public GroupColor(double l, double a, double b) {
		super(l, a, b);
		mARGB = RGBToCIELabConverter.convertCIELabToRGB(l, a, b);
		mApplicationList = new ArrayList<>();
	}

	public int getARGB() {
		return mARGB;
	}

	public void setARGB(int ARGB) {
		mARGB = ARGB;
	}

	public void setARGBColor(double l, double a, double b) {
		mARGB = RGBToCIELabConverter.convertCIELabToRGB(l, a, b);
	}

	public ArrayList<ApplicationInfoBundle> getApplicationList() {
		return mApplicationList;
	}

	public void setApplicationList(ArrayList<ApplicationInfoBundle> applicationList) {
		mApplicationList = applicationList;
	}

	public void addApplicationInfoBundle(ApplicationInfoBundle applicationInfoBundle) {
		mApplicationList.add(applicationInfoBundle);
	}
}