package edu.skku.inho.colorize.IconGroupingModule;

import android.content.Context;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import edu.skku.inho.colorize.Constants;
import edu.skku.inho.colorize.LockScreenDataProvider;

/**
 * Created by XEiN on 2/12/16.
 */
public class KMeans {
	private static final String TAG = "KMeans";
	//Min and Max X and Y
	private static final int MIN_COORDINATE = 0;
	private static final int MAX_COORDINATE = 10;
	//Number of Clusters. This metric should be related to the number of mColors
	private int NUM_CLUSTERS = Constants.DEFAULT_NUMBER_OF_GROUP_COLOR;
	private List<Color> mColors;
	private List<Group> mGroups;

	public KMeans(List<Color> colors) {
		mColors = colors;
		mGroups = new ArrayList<>();
	}

	//Initializes the process
	public void init(Context context) {
		//Create Groups
		NUM_CLUSTERS = LockScreenDataProvider.getInstance(context).getNumberOfGroupColors();

		//Set Random Centroids
		for (int i = 0; i < NUM_CLUSTERS; i++) {
			Group group = new Group(i);
			Color centroid = Color.createRandomPoint(MIN_COORDINATE, MAX_COORDINATE);
			group.setCentroid(centroid);
			mGroups.add(group);
		}

		//Print Initial state
		plotGroups();
	}

	private void plotGroups() {
		for (int i = 0; i < NUM_CLUSTERS; i++) {
			Group c = mGroups.get(i);
			c.plotGroup();
		}
	}

	//The process to calculate the K Means, with iterating method.
	public void calculate() {
		boolean finish = false;
		int iteration = 0;

		// Add in new data, one at a time, recalculating centroids with each new one.
		while (!finish) {
			//Clear group state
			clearGroups();

			List lastCentroids = getCentroids();

			//Assign mColors to the closer group
			assignGroup();

			//Calculate new centroids.
			calculateCentroids();

			iteration++;

			List currentCentroids = getCentroids();

			//Calculates total distance between new and old Centroids
			double distance = 0;

			for (int i = 0; i < lastCentroids.size(); i++) {
				distance += Color.distance((Color) lastCentroids.get(i), (Color) currentCentroids.get(i));
			}
			Log.d(TAG, "#################");
			Log.d(TAG, "Iteration: " + iteration);
			Log.d(TAG, "Centroid distances: " + distance);
			//plotGroups();

			if (distance == 0) {
				finish = true;
			}
		}
		//
	}

	private void clearGroups() {
		for (Group group : mGroups) {
			group.clear();
		}
	}

	private List<Color> getCentroids() {
		List<Color> centroids = new ArrayList<>(NUM_CLUSTERS);
		for (Group group : mGroups) {
			Color aux = group.getCentroid();
			Color color = new Color(aux.getL(), aux.getA(), aux.getB());
			centroids.add(color);
		}
		return centroids;
	}

	private void assignGroup() {
		double max = Double.MAX_VALUE;
		double min;
		int group = 0;
		double distance;

		for (Color color : mColors) {
			min = max;
			for (int i = 0; i < NUM_CLUSTERS; i++) {
				Group c = mGroups.get(i);
				distance = Color.distance(color, c.getCentroid());
				if (distance < min) {
					min = distance;
					group = i;
				}
			}
			color.setGroup(group);
			mGroups.get(group).addColor(color);
		}
	}

	private void calculateCentroids() {
		for (Group group : mGroups) {
			double sumL = 0;
			double sumA = 0;
			double sumB = 0;
			List<Color> colors = group.getColors();
			int nColors = colors.size();

			for (Color color : colors) {
				sumL += color.getL();
				sumA += color.getA();
				sumB += color.getB();
			}

			Color centroid = group.getCentroid();
			if (nColors > 0) {
				double newL = sumL / nColors;
				double newA = sumA / nColors;
				double newB = sumB / nColors;
				centroid.setL(newL);
				centroid.setA(newA);
				centroid.setB(newB);
			}
		}
	}

	public List<Group> getGroups() {
		return mGroups;
	}

	public static class Group {

		public List<Color> mColors;
		public Color mCentroid;
		public int mId;

		//Creates a new Group
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

		public void clear() {
			mColors.clear();
		}

		public void plotGroup() {
			Log.d(TAG, "Group: " + mId + " / Centroid: " + mCentroid);
			Log.d(TAG, "Centroid color ARGB code: " + RGBToCIELabConverter.convertLabToRGB(mCentroid.getL(), mCentroid.getA(), mCentroid.getB()));
			Log.d(TAG, "Points: ");
			for (Color color : mColors) {
				Log.d(TAG, "       " + color);
			}
		}
	}
}
