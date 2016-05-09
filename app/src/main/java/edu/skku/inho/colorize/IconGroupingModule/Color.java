package edu.skku.inho.colorize.IconGroupingModule;

import java.util.Random;

import edu.skku.inho.colorize.ApplicationInfoBundle;

/**
 * Created by In-Ho Han on 2/12/16.
 *
 * class that represents one point in CIE-L*a*b* color space.
 * it may also contain info of an application it represents if the color is not a standard color.
 */
public class Color {
	private double mL = 0;          // L* value for the color
	private double mA = 0;          // a* value for the color
	private double mB = 0;          // b* value for the color

	private ApplicationInfoBundle mApplicationInfoBundle;   // holds info of an application that the color represents

	/**
	 * Constructor
	 *
	 * @param l L* value for the color
	 * @param a a* value for the color
	 * @param b b* value for the color
	 */
	public Color(double l, double a, double b) {
		mL = l;
		mA = a;
		mB = b;
	}

	/**
	 * calculates the euclidean distance(in CIE-L*a*b* color space) between two colors.
	 * @param p         color(coordinate)
	 * @param centroid  centroid which is also a color
	 */
	protected static double distance(Color p, Color centroid) {
		return Math.sqrt(Math.pow((centroid.getL() - p.getL()), 2) + Math.pow((centroid.getA() - p.getA()), 2) + Math.pow((centroid.getB() - p.getB()), 2));
	}

	public double getL() {
		return mL;
	}

	public void setL(double l) {
		mL = l;
	}

	public double getA() {
		return mA;
	}

	public void setA(double a) {
		mA = a;
	}

	public double getB() {
		return mB;
	}

	public void setB(double b) {
		mB = b;
	}

	/**
	 * deprecated.
	 * creates random point.
	 * this method was used for random centroid generation.
	 * @param min
	 * @param max
	 **/
	protected static Color createRandomPoint(int min, int max) {
		Random r = new Random();
		double l = min + (max - min) * r.nextDouble();
		double a = min + (max - min) * r.nextDouble();
		double b = min + (max - min) * r.nextDouble();
		return new Color(l, a, b);
	}

	public ApplicationInfoBundle getApplicationInfoBundle() {
		return mApplicationInfoBundle;
	}

	public void setApplicationInfoBundle(ApplicationInfoBundle applicationInfoBundle) {
		mApplicationInfoBundle = applicationInfoBundle;
	}

	@Override
	public String toString() {
		return "(" + "L*a*b*: " + mL + ", " + mA + ", " + mB + ")";
	}
}
