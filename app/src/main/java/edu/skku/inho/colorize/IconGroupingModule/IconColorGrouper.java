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
 * Created by In-Ho Han on 3/1/16.
 *
 * Class that groups icons of all applications installed inside the device into 7 groups
 * and each group represents standard color.
 * Each application is allocated to one of 7 standard color groups based on its main color of its icon.
 * 7 standard colors are calculated with K-means algorithm.
 *
 * This class is designed with builder pattern. Therefore it can be instantiated with IconColorGrouper.Builder class.
 */
public class IconColorGrouper {
	private List<GroupColor> mGroupColorList;               // list of standard colors
	private List<ApplicationInfoBundle> mApplicationList;   // list of applications

	/**
	 * Constructor.
	 * This constructor cannot be called outside of this class. It is used only for IconColorGrouper.Builder class.
	 *
	 * @param applicationList
	 * @param groupColorList
	 */
	private IconColorGrouper(List<ApplicationInfoBundle> applicationList, List<GroupColor> groupColorList) {
		mApplicationList = applicationList;
		mGroupColorList = groupColorList;
	}

	/**
	 * Static method that returns Builder instance.
	 * This is a starting method to instantiated IconColorGrouper class.
	 * @param applicationInfoList list of application to be calculated to form 7 groups(standard colors)
	 * @param context context which this class is in.
	 * @return Builder instance
	 */
	public static Builder groupFrom(List<ResolveInfo> applicationInfoList, Context context) {
		return new Builder(applicationInfoList, context);
	}

	public List<GroupColor> getGroupColorList() {
		return mGroupColorList;
	}

	public List<ApplicationInfoBundle> getApplicationList() {
		return mApplicationList;
	}

	/**
	 * Inner class that builds IconColorGrouper class
	 */
	public static class Builder {
		private static final String TAG = "IconColorGrouper.Builder";

		private Context mContext;
		private List<ResolveInfo> mApplicationInfoList;         // list of applications(each item has full info of application)
		private List<ApplicationInfoBundle> mApplicationList;   // list of applications(light weighted list which means there are only info needed inside each item)

		// fixed 7 standard color for fixed color grouping mode
		private int[] mFixedGroupColorIds = {R.color.fixed_color_one, R.color.fixed_color_two, R.color.fixed_color_three, R.color.fixed_color_four, R.color.fixed_color_five, R.color.fixed_color_six, R.color.fixed_color_seven};

		private boolean mIsGroupingWithFixedColorModeInitialized = true;

		/**
		 * Constructor
		 * @param applicationInfoList list of application(each item has full info of application)
		 * @param context context which this class is in.
		 */
		public Builder(List<ResolveInfo> applicationInfoList, Context context) {
			mApplicationInfoList = applicationInfoList;
			mContext = context;
		}

		/**
		 * groups all application with 7 fixed standard colors and generates instance of IconColorGrouper class.
		 * Fixed standard color is the color that is assigned by the developer.
		 *
		 * @return IconColorGrouper instance
		 */
		public IconColorGrouper generateWithFixedGroupColor() {
			List<GroupColor> groupColorList = allocateExtractedColorsToGroupColors(loadFixedGroupColors(), makeExtractedMainColorList());
			return new IconColorGrouper(mApplicationList, groupColorList);
		}

		/**
		 * allocates main color extracted from an application's icon to one of 7 standard colors.
		 * It is grouping process. Allocating a color to a standard color also means application being
		 * grouped.
		 *
		 * @param groupColorList     7 standard colors
		 * @param extractedColorList list of main colors extracted from icons
		 * @return list of 7 standard colors
		 */
		private ArrayList<GroupColor> allocateExtractedColorsToGroupColors(ArrayList<GroupColor> groupColorList, ArrayList<Color> extractedColorList) {
			for (Color extractedColor : extractedColorList) {
				double minimumEuclideanDistance = Double.MAX_VALUE;
				int minimumEuclideanDistanceIndex = -1;

				for (int i = 0; i < groupColorList.size(); i++) {
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

		/**
		 * makes an ArrayList instance that contains 7 fixed standard colors.
		 * @return list of 7 fixed standard colors
		 */
		private ArrayList<GroupColor> loadFixedGroupColors() {
			if (mIsGroupingWithFixedColorModeInitialized) {
				ArrayList<GroupColor> groupColorList = new ArrayList<>();
				for (int i = 0; i < mFixedGroupColorIds.length; i++) {
					double[] CIELab;
					if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
						CIELab = RGBToCIELabConverter.convertRGBToCIELab(mContext.getResources().getColor(mFixedGroupColorIds[i]));
					} else {
						CIELab = RGBToCIELabConverter.convertRGBToCIELab(mContext.getColor(mFixedGroupColorIds[i]));

					}
					groupColorList.add(new GroupColor(CIELab[0], CIELab[1], CIELab[2]));
				}

				return groupColorList;
			} else {
				return null;
			}
		}

		/**
		 * makes an ArrayList instance that contains main colors that are extracted from applications' icons.
		 * @return list of extracted main color
		 */
		private ArrayList<Color> makeExtractedMainColorList() {
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
				extractMainColor(Palette.from(DrawableToBitmapConverter.convertToBitmap(tempDrawable, drawableSize, drawableSize)).generate(),
						applicationInfoBundle,
						extractedColorList);

				mApplicationList.add(applicationInfoBundle);
			}

			Log.i(TAG, ">>> Number of applications installed: " + mApplicationList.size());
			return extractedColorList;
		}

		/**
		 * sorts list of applications in alphabetical order
		 * @param applicationList list of applications installed inside a device
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

		/**
		 * extracts a main color from an icon of an application.
		 * The main color is highly populated color inside a bitmap image of the icon.
		 *
		 * @param palette                Palette instance that is used to extract highly populated color
		 * @param applicationInfoBundle  instance that has application info(Application icon image data, name of application, package name of application)
		 * @param extractedMainColorList ArrayList that has list of extracted main colors
		 */
		private void extractMainColor(Palette palette, ApplicationInfoBundle applicationInfoBundle, ArrayList<Color> extractedMainColorList) {
			/*Palette.Swatch vibrantSwatch = palette.getVibrantSwatch();
			Palette.Swatch lightVibrantSwatch = palette.getLightVibrantSwatch();
			Palette.Swatch darkVibrantSwatch = palette.getDarkVibrantSwatch();

			Palette.Swatch mutedSwatch = palette.getMutedSwatch();
			Palette.Swatch lightMutedSwatch = palette.getLightMutedSwatch();
			Palette.Swatch darkMutedSwatch = palette.getDarkMutedSwatch();

			Log.i(TAG, ">> Starting extracting seven colors from app: " + applicationInfoBundle.getApplicationName());

			if (vibrantSwatch != null) {
				//Log.i(TAG, "Extracting vibrant color...");
				extractedMainColorList.add(makeExtractedColorPoint(vibrantSwatch, applicationInfoBundle));
			}

			if (lightVibrantSwatch != null) {
				//Log.i(TAG, "Extracting light vibrant color...");
				extractedMainColorList.add(makeExtractedColorPoint(lightVibrantSwatch, applicationInfoBundle));
			}

			if (darkVibrantSwatch != null) {
				//Log.i(TAG, "Extracting dark vibrant color..");
				extractedMainColorList.add(makeExtractedColorPoint(darkVibrantSwatch, applicationInfoBundle));
			}

			if (mutedSwatch != null) {
				//Log.i(TAG, "Extracting muted color...");
				extractedMainColorList.add(makeExtractedColorPoint(mutedSwatch, applicationInfoBundle));
			}

			if (lightMutedSwatch != null) {
				//Log.i(TAG, "Extracting light muted color...");
				extractedMainColorList.add(makeExtractedColorPoint(lightMutedSwatch, applicationInfoBundle));
			}

			if (darkMutedSwatch != null) {
				//Log.i(TAG, "Extracting dark muted color...");
				extractedMainColorList.add(makeExtractedColorPoint(darkMutedSwatch, applicationInfoBundle));
			}
*/
			//Log.i(TAG, "Extracting highly populated color...");
			if (palette.getSwatches().size() > 0) {
				//Log.d(TAG, applicationInfoBundle.getApplicationName());
				Color color = HighlyPopulatedColorExtractor.extractHighlyPopulatedColor(palette.getSwatches());
				color.setApplicationInfoBundle(applicationInfoBundle);
				extractedMainColorList.add(color);
			} else {
				//Log.d(TAG, "Failed to extract color : " + applicationInfoBundle.getApplicationName());
			}
			//Log.i(TAG, ">> Extraction completed");
		}

		/**
		 * groups all application with 7 calculated standard colors and generates instance of IconColorGrouper class.
		 * @return IconColorGrouper instance
		 */
		public IconColorGrouper generateWithCalculatedGroupColor() {
			List<GroupColor> groupColorList = makeGroupColorList();
			return new IconColorGrouper(mApplicationList, groupColorList);
		}

		/**
		 * makes an ArrayList instance of list of 7 standard colors
		 * @return list of 7 standard colors
		 */
		private ArrayList<GroupColor> makeGroupColorList() {
			ArrayList<GroupColor> groupColorList = new ArrayList<>();
			for (KMeans.Group group : groupExtractedMainColorIntoSevenGroups(makeExtractedMainColorList())) {
				groupColorList.add(makeGroupColorWithCentroid(group));
			}
			return groupColorList;
		}

		/**
		 * groups main colors extracted from applications' icons into 7 groups using K-means algorithm.
		 * This is grouping process. 7 standard colors' objects(GroupColor class) are not instantiated at this process.
		 * @param extractedMainColorList list of main colors extracted from applications' icons
		 * @return 7 groups made by K-means algorithm.
		 */
		private List<KMeans.Group> groupExtractedMainColorIntoSevenGroups(ArrayList<Color> extractedMainColorList) {
			Log.i(TAG, ">>> Starting color grouping...");
			//KMeans kMeans = new KMeans(LockScreenDataManager.getInstance().getExtractedColorPointList());
			KMeans kMeans = new KMeans(extractedMainColorList);
			kMeans.init(mContext);
			kMeans.calculate();
			Log.i(TAG, ">>> Grouping colors completed");
			return kMeans.getGroups();
		}

		/**
		 * makes an instance of standard color with centroid of the group made with main colors.
		 * The coordinate of centroid is based on CIE-L*a*b* color space.
		 * @param group the group made by K-means algorithm
		 * @return GroupColor instance that is instantiated by centroid of the group
		 */
		private GroupColor makeGroupColorWithCentroid(KMeans.Group group) {
			Color centroid = group.getCentroid();
			GroupColor groupColor = new GroupColor(centroid.getL(), centroid.getA(), centroid.getB());

			boolean isDuplicate;
			for (Color color : group.getColors()) {
				isDuplicate = false;
				ApplicationInfoBundle temp = color.getApplicationInfoBundle();
				ArrayList<ApplicationInfoBundle> tempList = groupColor.getApplicationList();
				for (int i = 0; i < tempList.size(); i++) {
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
