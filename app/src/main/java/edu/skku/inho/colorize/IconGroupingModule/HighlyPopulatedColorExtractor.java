package edu.skku.inho.colorize.IconGroupingModule;

import android.support.v7.graphics.Palette;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Created by XEiN on 2/11/16.
 */
public class HighlyPopulatedColorExtractor {

	public final static Comparator<Palette.Swatch> mSwatchColorPopulationComparator = new Comparator<Palette.Swatch>() {

		@Override
		public int compare(Palette.Swatch o1, Palette.Swatch o2) {
			return o2.getPopulation() < o1.getPopulation() ? -1 : (o2.getPopulation() == o1.getPopulation() ? 0 : 1);
		}
	};

	public static Color extractHighlyPopulatedColor(List<Palette.Swatch> swatches) {
		List<Palette.Swatch> tempSwatches = new ArrayList<>(swatches);

		Collections.sort(tempSwatches, mSwatchColorPopulationComparator);

		Palette.Swatch highlyPopulatedColorSwatch = tempSwatches.get(0);

		double[] CIELab = RGBToCIELabConverter.convertRGBToLab(highlyPopulatedColorSwatch.getRgb());

		return new Color(CIELab[0], CIELab[1], CIELab[2]);
	}
}
