package edu.skku.inho.colorize.IconGroupingModule;

import android.graphics.Color;

/**
 * Created by XEiN on 2/11/16.
 */
public class RGBToCIELabConverter {
	private static final String TAG = "RGBToCIELabConverter";

	public static double[] convertRGBToLab(int argbColor) {
		double r = (double) Color.red(argbColor) / 255D;          //R from 0 to 255
		double g = (double) Color.green(argbColor) / 255D;        //G from 0 to 255
		double b = (double) Color.blue(argbColor) / 255D;         //B from 0 to 255

		if (r > 0.04045) {
			r = Math.pow(((r + 0.055) / 1.055), 2.4);
		} else {
			r = r / 12.92;
		}

		if (g > 0.04045) {
			g = Math.pow(((g + 0.055) / 1.055), 2.4);
		} else {
			g = g / 12.92;
		}

		if (b > 0.04045) {
			b = Math.pow(((b + 0.055) / 1.055), 2.4);
		} else {
			b = b / 12.92;
		}

		r = r * 100D;
		g = g * 100D;
		b = b * 100D;

		//Observer. = 2°, Illuminant = D65
		double x = r * 0.4124 + g * 0.3576 + b * 0.1805;
		double y = r * 0.2126 + g * 0.7152 + b * 0.0722;
		double z = r * 0.0193 + g * 0.1192 + b * 0.9505;

		double refX = 95.047;
		double refY = 100.000;
		double refZ = 108.883;

		x = x / refX;          //refX =  95.047   Observer= 2°, Illuminant= D65
		y = y / refY;          //refY = 100.000
		z = z / refZ;          //refZ = 108.883

		if (x > 0.008856) {
			x = Math.pow(x, (1D / 3D));
		} else {
			x = (7.787 * x) + (16D / 116D);
		}

		if (y > 0.008856) {
			y = Math.pow(y, (1D / 3D));
		} else {
			y = (7.787 * y) + (16D / 116D);
		}

		if (z > 0.008856) {
			z = Math.pow(z, (1D / 3D));
		} else {
			z = (7.787 * z) + (16D / 116D);
		}

		double[] CIELab = new double[3];
		CIELab[0] = (116D * y) - 16D;     //CIE-L*
		CIELab[1] = 500D * (x - y);      //CIE-a*
		CIELab[2] = 200D * (y - z);      //CIE-b*

		//Log.i(TAG, "L*: " + CIELab[0] + " a*: " + CIELab[1] + " b*: " + CIELab[2]);

		return CIELab;
	}

	public static int convertLabToRGB(double CIEL, double CIEa, double CIEb) {
		double y = (CIEL + 16D) / 116D;
		double x = CIEa / 500D + y;
		double z = y - CIEb / 200D;

		if (Math.pow(y, 3D) > 0.008856) {
			y = Math.pow(y, 3D);
		} else {
			y = (y - 16D / 116D) / 7.787;
		}

		if (Math.pow(x, 3D) > 0.008856) {
			x = Math.pow(x, 3D);
		} else {
			x = (x - 16D / 116D) / 7.787;
		}

		if (Math.pow(z, 3D) > 0.008856) {
			z = Math.pow(z, 3D);
		} else {
			z = (z - 16D / 116D) / 7.787;
		}

		x = 95.047 * x;     //ref_X =  95.047     Observer= 2°, Illuminant= D65
		y = 100.000 * y;    //ref_Y = 100.000
		z = 108.883 * z;    //ref_Z = 108.883

		x = x / 100D;        //X from 0 to  95.047      (Observer = 2°, Illuminant = D65)
		y = y / 100D;        //Y from 0 to 100.000
		z = z / 100D;        //Z from 0 to 108.883

		double r = x * 3.2406 + y * -1.5372 + z * -0.4986;
		double g = x * -0.9689 + y * 1.8758 + z * 0.0415;
		double b = x * 0.0557 + y * -0.2040 + z * 1.0570;

		if (r > 0.0031308) {
			r = 1.055 * Math.pow(r, (1D / 2.4)) - 0.055;
		} else {
			r = 12.92 * r;
		}

		if (g > 0.0031308) {
			g = 1.055 * Math.pow(g, (1D / 2.4)) - 0.055;
		} else {
			g = 12.92 * g;
		}

		if (b > 0.0031308) {
			b = 1.055 * Math.pow(b, (1D / 2.4)) - 0.055;
		} else {
			b = 12.92 * b;
		}

		r = r * 255;
		g = g * 255;
		b = b * 255;

		return Color.argb(255, (int) r, (int) g, (int) b);
	}
}
