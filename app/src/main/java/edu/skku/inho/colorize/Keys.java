package edu.skku.inho.colorize;

/**
 * Created by XEiN on 2/18/16.
 *
 * This class contains key values for intent, shared preference
 */
public class Keys {
	/**
	 * key for shared preference in setting page(@link SettingActivity)
	 */
	public static final String GROUPING_MODE = "edu.skku.inho.colorize.grouping_mode";

	/**
	 * key for intent in setting page
	 */
	public static final String IS_LOCK_SCREEN_RUNNING = "edu.skku.inho.colorize.is_lock_screen_running";

	/**
	 * key for shared preference in lock screen page(@link MainActivity)
	 */
	public static final String IS_COLOR_DATA_READY = "edu.skku.inho.colorize.is_color_data_ready";

	/**
	 * keys for intent in update service
	 */
	// intent filter
	public static final String UPDATE_SERVICE_BROADCAST = "edu.skku.inho.colorize.update_service_broadcast";
	// action
	public static final String UPDATE_SERVICE_MESSAGE = "edu.skku.inho.colorize.update_service_message";

	/**
	 * key for application list change checking period in setting page
	 */
	public static final String CHECKING_PERIOD_INDEX = "edu.skku.inho.colorize.checking_period_index";
}
