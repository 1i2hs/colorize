package edu.skku.inho.colorize.IconGroupingModule;

import java.util.Random;

import edu.skku.inho.colorize.ApplicationInfoBundle;

/**
 * Created by XEiN on 2/12/16.
 */
public class Color {
	private double mL = 0;
	private double mA = 0;
	private double mB = 0;
	private int mGroupNumber = 0;

	private ApplicationInfoBundle mApplicationInfoBundle;

	public Color(double l, double a, double b) {
		mL = l;
		mA = a;
		mB = b;
	}

	//Calculates the distance between two mColors.
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

	//Creates random point
	protected static Color createRandomPoint(int min, int max) {
		Random r = new Random();
		double l = min + (max - min) * r.nextDouble();
		double a = min + (max - min) * r.nextDouble();
		double b = min + (max - min) * r.nextDouble();
		return new Color(l, a, b);
	}

	public void setGroup(int groupNumber) {
		mGroupNumber = groupNumber;
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