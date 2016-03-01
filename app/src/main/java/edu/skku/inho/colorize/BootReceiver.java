package edu.skku.inho.colorize;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class BootReceiver extends BroadcastReceiver {
	public BootReceiver() {
	}

	@Override
	public void onReceive(Context context, Intent intent) {
		if (LockScreenDataProvider.getInstance(context).getGroupingMode() != Constants.GROUPING_MODE_NOT_SELECTED) {
			Intent i = new Intent(context, UpdateService.class);
			context.startService(i);
		}
	}
}
