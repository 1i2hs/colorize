package edu.skku.inho.colorize.IconGroupingModule;

import android.support.v7.graphics.Palette;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Created by In-Ho Han on 2/11/16.
 *
 * class that extracts a color that is highly populated among list of color swatches(Palette.Swatch class).
 */
public class HighlyPopulatedColorExtractor {

	/**
	 * Comparator for Collections.sort() method.
	 * It is coded to make sort() method to sort list of color swatches in descending order of each population.
	 */
	public final static Comparator<Palette.Swatch> mSwatchColorPopulationComparator = new Comparator<Palette.Swatch>() {

		@Override
		public int compare(Palette.Swatch o1, Palette.Swatch o2) {
			return o2.getPopulation() < o1.getPopulation() ? -1 : (o2.getPopulation() == o1.getPopulation() ? 0 : 1);
		}
	};

	/**
	 * sorts a list of color swatches in descending order of each population
	 * and returns the highly populated color.
	 * @param swatches list of color swatches
	 * @return highly populated color
	 */
	public static Color extractHighlyPopulatedColor(List<Palette.Swatch> swatches) {
		List<Palette.Swatch> tempSwatches = new ArrayList<>(swatches);

		Collections.sort(tempSwatches, mSwatchColorPopulationComparator);

		Palette.Swatch highlyPopulatedColorSwatch = tempSwatches.get(0);

		double[] CIELab = RGBToCIELabConverter.convertRGBToCIELab(highlyPopulatedColorSwatch.getRgb());

		return new Color(CIELab[0], CIELab[1], CIELab[2]);
	}
}
