package edu.skku.inho.colorize.IconGroupingModule;

import android.content.Context;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import edu.skku.inho.colorize.Constants;
import edu.skku.inho.colorize.LockScreenDataManager;
import edu.skku.inho.colorize.R;

/**
 * Created by In-Ho Han on 2/12/16.
 *
 * Class that groups main colors into 7 groups with calculating a centroid of each group.
 * The euclidean distance between colors are calculated inside CIE-L*a*b* color space.
 */
public class KMeans {
	private static final String TAG = "KMeans";

	private int numberOfGroups = Constants.DEFAULT_NUMBER_OF_GROUP_COLOR; // number of groups. This metric should be related to the number of mColors(object)
	private List<Color> mColors; // list of main colors
	private List<Group> mGroups; // list of groups formed with main colors

	// fixed 7 standard color for fixed color grouping mode
	private int[] mFixedGroupColorIds = {R.color.fixed_color_one, R.color.fixed_color_two, R.color.fixed_color_three, R.color.fixed_color_four, R.color.fixed_color_five, R.color.fixed_color_six, R.color.fixed_color_seven};

	/**
	 * Constructor
	 *
	 * @param colors list of main colors extracted from icons of applications
	 */
	public KMeans(List<Color> colors) {
		mColors = colors;
		mGroups = new ArrayList<>();
	}

	/**
	 * initializes the grouping process
	 * @param context context which this class is in
	 */
	public void init(Context context) {
		// get number of groups from LockScreenProvider and allocate it to numberOfGroups
		numberOfGroups = LockScreenDataManager.getInstance(context).getNumberOfGroupColors();

		//Set fixed point to begin grouping/clustering
		for (int i = 0; i < numberOfGroups; i++) {
			Group group = new Group(i);
			//Color centroid = Color.createRandomPoint(MIN_COORDINATE, MAX_COORDINATE);
			double[] CIELab = RGBToCIELabConverter.convertRGBToCIELab(context.getResources().getColor(mFixedGroupColorIds[i]));
			Color centroid = new Color(CIELab[0], CIELab[1], CIELab[2]);
			group.setCentroid(centroid);
			mGroups.add(group);
		}

		//plotGroups();
	}

	/**
	 * prints initial state of groups to the Android Logcat.
	 * This method is for debugging.
	 */
	private void plotGroups() {
		for (int i = 0; i < numberOfGroups; i++) {
			Group c = mGroups.get(i);
			c.plotGroup();
		}
	}

	/**
	 * calculates the K-means with main colors extracted from icons of applications.
	 * Iterates until there is no change of all centroids.
	 */
	public void calculate() {
		boolean finish = false;
		int iteration = 0;

		//  in new data, one at a time, recalculating centroids with each new one
		while (!finish) {
			// clears group state
			clearGroups();

			List lastCentroids = getCentroids();

			// allocates main colors to the closer group(centroid)
			allocateGroup();

			// calculates new centroids
			calculateCentroids();

			// increases number of iteration
			iteration++;

			List currentCentroids = getCentroids();

			/* calculates total distance between new and old centroids
			 * to check the changes of centroid. This is required to stop
			 * the iteration */
			double distance = 0;

			for (int i = 0; i < lastCentroids.size(); i++) {
				distance += Color.distance((Color) lastCentroids.get(i), (Color) currentCentroids.get(i));
			}
			/* end of calculation */
			Log.d(TAG, "======================================================");
			Log.d(TAG, "Iteration: " + iteration);
			Log.d(TAG, "Centroid distances: " + distance);
			//plotGroups();

			// if there is no change for the positions of centroids stop the iteration
			if (distance == 0) {
				finish = true;
			}
		}
	}

	/**
	 * empties each group where colors are allocated inside
	 */
	private void clearGroups() {
		for (Group group : mGroups) {
			group.clear();
		}
	}

	/**
	 * gathers centroids of all groups and returns the result.
	 * @return calculated centroids
	 */
	private List<Color> getCentroids() {
		List<Color> centroids = new ArrayList<>(numberOfGroups);
		for (Group group : mGroups) {
			Color centroid = group.getCentroid();
			Color color = new Color(centroid.getL(), centroid.getA(), centroid.getB());
			centroids.add(color);
		}
		return centroids;
	}

	/**
	 * allocates each main color to the closest centroid and add the color to a group where the centroid belongs.
	 */
	private void allocateGroup() {
		double max = Double.MAX_VALUE;
		double min;
		int group = 0;
		double distance;

		/* for each main color calculate the euclidean distance between all centroids
		 * and allocate the color into the group where the closest centroid belongs.
		 */
		for (Color color : mColors) {
			min = max;
			for (int i = 0; i < numberOfGroups; i++) {
				Group singeGroup = mGroups.get(i);
				distance = Color.distance(color, singeGroup.getCentroid());
				if (distance < min) {
					min = distance;
					group = i;
				}
			}
			mGroups.get(group).addColor(color);
		}
	}

	/**
	 * calculates new centroids with allocated main colors.
	 */
	private void calculateCentroids() {
		for (Group group : mGroups) {
			double sumL = 0;
			double sumA = 0;
			double sumB = 0;
			List<Color> colors = group.getColors();
			int numberOfColors = colors.size();

			// adds all L* values together, a* values together, b* values together to calculate a new centroid
			for (Color color : colors) {
				sumL += color.getL();
				sumA += color.getA();
				sumB += color.getB();
			}

			/* calculates new centroid by dividing total L*, total a* value, and total b* value with number of main colors
			 * allocated to the current group. After that sets the newly calculated coordinate(L*, a*, b* value) to centroid instance
			 * of the current group.
			 */
			Color centroid = group.getCentroid();
			if (numberOfColors > 0) {
				double newL = sumL / numberOfColors;
				double newA = sumA / numberOfColors;
				double newB = sumB / numberOfColors;
				centroid.setL(newL);
				centroid.setA(newA);
				centroid.setB(newB);
			}
		}
	}

	public List<Group> getGroups() {
		return mGroups;
	}

	/**
	 * Inner class that represents group that contains main colors.
	 */
	public static class Group {
		public List<Color> mColors; // list of main colors inside this group
		public Color mCentroid;     // centroid of this group
		public int mId;             // unique id for this group

		//Creates a new Group

		/**
		 * Constructor
		 * @param id unique id for this group
		 */
		public Group(int id) {
			mId = id;
			mColors = new ArrayList<>();
			mCentroid = null;
		}

		public List<Color> getColors() {
			return mColors;
		}

		public void setColors(List<Color> colors) {
			this.mColors = colors;
		}

		public void addColor(Color color) {
			mColors.add(color);
		}

		public Color getCentroid() {
			return mCentroid;
		}

		public void setCentroid(Color centroid) {
			this.mCentroid = centroid;
		}

		public int getId() {
			return mId;
		}

		/**
		 * clears main colors inside this group.
		 */
		public void clear() {
			mColors.clear();
		}

		/**
		 * prints current status of this group to the Android Logcat.
		 * This method is for debugging.
		 */
		public void plotGroup() {
			Log.d(TAG, "Group: " + mId + " / Centroid: " + mCentroid);
			Log.d(TAG, "Centroid color ARGB code: " + RGBToCIELabConverter.convertCIELabToRGB(mCentroid.getL(), mCentroid.getA(), mCentroid.getB()));
			Log.d(TAG, "Points: ");
			for (Color color : mColors) {
				Log.d(TAG, "       " + color);
			}
		}
	}
}
