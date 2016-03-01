package edu.skku.inho.colorize.IconGroupingModule;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.ResolveInfo;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.v7.graphics.Palette;
import android.util.Log;
import android.util.TypedValue;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import edu.skku.inho.colorize.ApplicationInfoBundle;
import edu.skku.inho.colorize.R;

/**
 * Created by XEiN on 3/1/16.
 */
public class IconColorGrouper {
	private List<GroupColor> mGroupColorList;
	private List<ApplicationInfoBundle> mApplicationList;

	public IconColorGrouper(List<ApplicationInfoBundle> applicationList, List<GroupColor> groupColorList) {
		mApplicationList = applicationList;
		mGroupColorList = groupColorList;
	}

	public static Builder groupFrom(List<ResolveInfo> applicationInfoList, Context context) {
		return new Builder(applicationInfoList, context);
	}

	public List<GroupColor> getGroupColorList() {
		return mGroupColorList;
	}

	public List<ApplicationInfoBundle> getApplicationList() {
		return mApplicationList;
	}

	public static class Builder {
		private static final String TAG = "IconColorGrouper.Builder";

		private Context mContext;
		private List<ResolveInfo> mApplicationInfoList;

		private List<ApplicationInfoBundle> mApplicationList;

		private int[] mFixedGroupColorIds = {R.color.fixed_color_one, R.color.fixed_color_two, R.color.fixed_color_three, R.color.fixed_color_four, R.color.fixed_color_five, R.color.fixed_color_six, R.color.fixed_color_seven, R.color.fixed_color_eight};

		private boolean mIsGroupingWithFixedColorModeInitialized = true;

		public Builder(List<ResolveInfo> applicationInfoList, Context context) {
			mApplicationInfoList = applicationInfoList;
			mContext = context;
		}

		public IconColorGrouper generateWithFixedColor() {
			List<GroupColor> groupColorList = matchExtractedColorsToGroupColors(loadFixedGroupColors(), makeExtractedColorPointList());
			return new IconColorGrouper(mApplicationList, groupColorList);
		}

		private ArrayList<GroupColor> matchExtractedColorsToGroupColors(ArrayList<GroupColor> groupColorList, ArrayList<Color> extractedColorList) {
			for (Color extractedColor : extractedColorList) {
				double minimumEuclideanDistance = Double.MAX_VALUE;
				int minimumEuclideanDistanceIndex = -1;

				for (int i = 0; i < groupColorList.size(); i++) {
					//double temp = computeEuclideanDistanceBetweenTwoColors(groupColorList.get(i), extractedColor);
					double temp = Color.distance(groupColorList.get(i), extractedColor);
					if (temp < minimumEuclideanDistance) {
						minimumEuclideanDistance = temp;
						minimumEuclideanDistanceIndex = i;
					}
					if (i == groupColorList.size() - 1) {
						ArrayList<ApplicationInfoBundle> applicationList = groupColorList.get(minimumEuclideanDistanceIndex).getApplicationList();
						boolean isApplicationAlreadyInsideList = false;
						for (int j = 0; j < applicationList.size(); j++) {
							if (extractedColor.getApplicationInfoBundle().getApplicationName().equals(applicationList.get(j).getApplicationName())) {
								isApplicationAlreadyInsideList = true;
								break;
							}
						}
						if (!isApplicationAlreadyInsideList) {
							groupColorList.get(minimumEuclideanDistanceIndex).addApplicationInfoBundle(extractedColor.getApplicationInfoBundle());
						}
					}
				}
			}
			for (GroupColor groupColor : groupColorList) {
				groupColor.setApplicationList(sortApplicationInAlphabeticalOrder(groupColor.getApplicationList()));
			}

			return groupColorList;
		}

		private ArrayList<GroupColor> loadFixedGroupColors() {
			if (mIsGroupingWithFixedColorModeInitialized) {
				ArrayList<GroupColor> groupColorList = new ArrayList<>();
				for (int i = 0; i < mFixedGroupColorIds.length; i++) {
					double[] CIELab;
					if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
						CIELab = RGBToCIELabConverter.convertRGBToLab(mContext.getResources().getColor(mFixedGroupColorIds[i]));
					} else {
						CIELab = RGBToCIELabConverter.convertRGBToLab(mContext.getColor(mFixedGroupColorIds[i]));

					}
					groupColorList.add(new GroupColor(CIELab[0], CIELab[1], CIELab[2]));
				}

				return groupColorList;
			} else {
				return null;
			}
		}

		private ArrayList<Color> makeExtractedColorPointList() {
			mApplicationList = new ArrayList<>();
			ArrayList<Color> extractedColorList = new ArrayList<>();
			int drawableSize = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 56, mContext.getResources().getDisplayMetrics());

			for (ResolveInfo resolveInfo : mApplicationInfoList) {
				ApplicationInfo temp = resolveInfo.activityInfo.applicationInfo;
				ApplicationInfoBundle applicationInfoBundle = new ApplicationInfoBundle();

				Drawable tempDrawable = temp.loadIcon(mContext.getPackageManager());
				applicationInfoBundle.setApplicationIcon(tempDrawable);
				applicationInfoBundle.setApplicationPackageName(temp.packageName);
				applicationInfoBundle.setApplicationName(temp.loadLabel(mContext.getPackageManager()).toString());
				//applicationInfoBundle.setIntentForPackage(getPackageManager().getLaunchIntentForPackage(temp.packageName));
				extractSevenColors(Palette.from(DrawableToBitmapConverter.convertToBitmap(tempDrawable, drawableSize, drawableSize)).generate(),
						applicationInfoBundle,
						extractedColorList);

				mApplicationList.add(applicationInfoBundle);
			}

			Log.i(TAG, ">>> Number of applications installed: " + mApplicationList.size());
			return extractedColorList;
		}

		/**
		 * must be merged with equal method in splash activity
		 *
		 * @param applicationList
		 * @return
		 */
		private ArrayList<ApplicationInfoBundle> sortApplicationInAlphabeticalOrder(ArrayList<ApplicationInfoBundle> applicationList) {
			Collections.sort(applicationList, new Comparator<ApplicationInfoBundle>() {
				@Override
				public int compare(ApplicationInfoBundle o1, ApplicationInfoBundle o2) {
					return o1.getApplicationName().compareToIgnoreCase(o2.getApplicationName());
				}
			});
			return applicationList;
		}

		private void extractSevenColors(Palette palette, ApplicationInfoBundle applicationInfoBundle, ArrayList<Color> extractedColorList) {
			Palette.Swatch vibrantSwatch = palette.getVibrantSwatch();
			Palette.Swatch lightVibrantSwatch = palette.getLightVibrantSwatch();
			Palette.Swatch darkVibrantSwatch = palette.getDarkVibrantSwatch();

			Palette.Swatch mutedSwatch = palette.getMutedSwatch();
			Palette.Swatch lightMutedSwatch = palette.getLightMutedSwatch();
			Palette.Swatch darkMutedSwatch = palette.getDarkMutedSwatch();

			Log.i(TAG, ">> Starting extracting seven colors from app: " + applicationInfoBundle.getApplicationName());

			if (vibrantSwatch != null) {
				//Log.i(TAG, "Extracting vibrant color...");
				extractedColorList.add(makeExtractedColorPoint(vibrantSwatch, applicationInfoBundle));
			}

			if (lightVibrantSwatch != null) {
				//Log.i(TAG, "Extracting light vibrant color...");
				extractedColorList.add(makeExtractedColorPoint(lightVibrantSwatch, applicationInfoBundle));
			}

			if (darkVibrantSwatch != null) {
				//Log.i(TAG, "Extracting dark vibrant color..");
				extractedColorList.add(makeExtractedColorPoint(darkVibrantSwatch, applicationInfoBundle));
			}

			if (mutedSwatch != null) {
				//Log.i(TAG, "Extracting muted color...");
				extractedColorList.add(makeExtractedColorPoint(mutedSwatch, applicationInfoBundle));
			}

			if (lightMutedSwatch != null) {
				//Log.i(TAG, "Extracting light muted color...");
				extractedColorList.add(makeExtractedColorPoint(lightMutedSwatch, applicationInfoBundle));
			}

			if (darkMutedSwatch != null) {
				//Log.i(TAG, "Extracting dark muted color...");
				extractedColorList.add(makeExtractedColorPoint(darkMutedSwatch, applicationInfoBundle));
			}

			//Log.i(TAG, "Extracting highly populated color...");
			if (palette.getSwatches().size() > 0) {
				//Log.d(TAG, applicationInfoBundle.getApplicationName());
				Color color = HighlyPopulatedColorExtractor.extractHighlyPopulatedColor(palette.getSwatches());
				color.setApplicationInfoBundle(applicationInfoBundle);
				extractedColorList.add(color);
			} else {
				//Log.d(TAG, "Failed to extract color : " + applicationInfoBundle.getApplicationName());
			}
			//Log.i(TAG, ">> Extraction completed");
		}

		private Color makeExtractedColorPoint(Palette.Swatch swatch, ApplicationInfoBundle applicationInfoBundle) {
			double[] cieLabColor = RGBToCIELabConverter.convertRGBToLab(swatch.getRgb());
			Color color = new Color(cieLabColor[0], cieLabColor[1], cieLabColor[2]);
			color.setApplicationInfoBundle(applicationInfoBundle);
			return color;
		}

		public IconColorGrouper generateWithVariableColor() {
			List<GroupColor> groupColorList = makeGroupColorList();
			return new IconColorGrouper(mApplicationList, groupColorList);
		}

		private ArrayList<GroupColor> makeGroupColorList() {
			ArrayList<GroupColor> groupColorList = new ArrayList<>();
			for (KMeans.Group group : groupExtractedColorPointsIntoEightColorPoints(makeExtractedColorPointList())) {
				groupColorList.add(mapGroupToGroupColor(group));
			}
			return groupColorList;
		}

		private List<KMeans.Group> groupExtractedColorPointsIntoEightColorPoints(ArrayList<Color> extractedColorList) {
			Log.i(TAG, ">>> Starting color grouping...");
			//KMeans kMeans = new KMeans(LockScreenDataProvider.getInstance().getExtractedColorPointList());
			KMeans kMeans = new KMeans(extractedColorList);
			kMeans.init(mContext);
			kMeans.calculate();
			Log.i(TAG, ">>> Grouping colors completed");
			return kMeans.getGroups();
		}

		private GroupColor mapGroupToGroupColor(KMeans.Group group) {
			Color centroid = group.getCentroid();
			GroupColor groupColor = new GroupColor(centroid.getL(), centroid.getA(), centroid.getB());

			boolean isDuplicate;
			for (Color color : group.getColors()) {
				isDuplicate = false;
				ApplicationInfoBundle temp = color.getApplicationInfoBundle();
				ArrayList<ApplicationInfoBundle> tempList = groupColor.getApplicationList();
				int tempListSize = tempList.size();
				for (int i = 0; i < tempListSize; i++) {
					if (temp.getApplicationName().equals(tempList.get(i).getApplicationName())) {
						isDuplicate = true;
						break;
					}
				}
				if (!isDuplicate) {
					groupColor.addApplicationInfoBundle(temp);
				}
			}

			return groupColor;
		}
	}
}
