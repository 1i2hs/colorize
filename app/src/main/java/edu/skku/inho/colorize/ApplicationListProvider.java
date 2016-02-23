package edu.skku.inho.colorize;

import java.util.ArrayList;

/**
 * Created by XEiN on 2/11/16.
 */
public class ApplicationListProvider {
	private static final String TAG = "ApplicationListProvider";
	private static ArrayList<ApplicationInfoBundle> mApplicationList;
	private static ArrayList<GroupColor> mGroupColorList;

	private int mNumberOfGroupColors = Constants.DEFAULT_NUMBER_OF_GROUP_COLOR;

	private ApplicationListProvider() {}

	public static ApplicationListProvider initInstance() {
		return getInstance();
	}

	public static ApplicationListProvider getInstance() {
		return Singleton.instance;
	}

	public ArrayList<ApplicationInfoBundle> getApplicationList() {
		return mApplicationList;
	}

	public void setApplicationList(ArrayList<ApplicationInfoBundle> applicationList) {
		mApplicationList = applicationList;
	}

	public synchronized ArrayList<GroupColor> getGroupColorList() {
		return mGroupColorList;
	}

	public synchronized void setGroupColorList(ArrayList<GroupColor> groupColorPointList) {
		mGroupColorList = groupColorPointList;
	}

	public GroupColor getClusterPoint(String index) {
		switch (index) {
			case GroupColor.FIRST_COLOR:
				return mGroupColorList.get(0);
			case GroupColor.SECOND_COLOR:
				return mGroupColorList.get(1);
			case GroupColor.THIRD_COLOR:
				return mGroupColorList.get(2);
			case GroupColor.FOURTH_COLOR:
				return mGroupColorList.get(3);
			case GroupColor.FIFTH_COLOR:
				return mGroupColorList.get(4);
			case GroupColor.SIXTH_COLOR:
				return mGroupColorList.get(5);
			case GroupColor.SEVENTH_COLOR:
				return mGroupColorList.get(6);
			case GroupColor.EIGHTH_COLOR:
				return mGroupColorList.get(7);
			default:
				return null;
		}
	}

	public int getNumberOfGroupColors() {
		return mNumberOfGroupColors;
	}

	public void setNumberOfGroupColors(int numberOfGroupColors) {
		mNumberOfGroupColors = numberOfGroupColors;
	}

	private static class Singleton {
		private static final ApplicationListProvider instance = new ApplicationListProvider();
	}
}
