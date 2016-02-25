package edu.skku.inho.colorize.IconSortingModule;

import java.util.ArrayList;

import edu.skku.inho.colorize.ApplicationInfoBundle;

/**
 * Created by XEiN on 2/11/16.
 */
public class GroupColor {
	public static final String FIRST_COLOR = "first_color";
	public static final String SECOND_COLOR = "second_color";
	public static final String THIRD_COLOR = "third_color";
	public static final String FOURTH_COLOR = "fourth_color";
	public static final String FIFTH_COLOR = "fifth_color";
	public static final String SIXTH_COLOR = "sixth_color";
	public static final String SEVENTH_COLOR = "seventh_color";
	public static final String EIGHTH_COLOR = "eighth_color";

	private double[] mCIELabColor;
	private int mARGBColor;
	private ArrayList<ApplicationInfoBundle> mApplicationList;

	public GroupColor(int argbColor) {
		mARGBColor = argbColor;
		mApplicationList = new ArrayList<>();
	}

	public GroupColor(double CIEL, double CIEa, double CIEb) {
		mARGBColor = RGBToCIELabConverter.convertLabToRGB(CIEL, CIEa, CIEb);
		mApplicationList = new ArrayList<>();
	}

	public GroupColor(double[] CIELabColor) {
		mCIELabColor = CIELabColor;
		mARGBColor = RGBToCIELabConverter.convertLabToRGB(CIELabColor[0], CIELabColor[1], CIELabColor[2]);
		mApplicationList = new ArrayList<>();
	}

	public int getARGBColor() {
		return mARGBColor;
	}

	public void setARGBColor(int ARGBColor) {
		mARGBColor = ARGBColor;
	}

	public void setARGBColor(double CIEL, double CIEa, double CIEb) {
		mARGBColor = RGBToCIELabConverter.convertLabToRGB(CIEL, CIEa, CIEb);
	}

	public double[] getCIELabColor() {
		return mCIELabColor;
	}

	public void setCIELabColor(double[] CIELabColor) {
		mCIELabColor = CIELabColor;
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