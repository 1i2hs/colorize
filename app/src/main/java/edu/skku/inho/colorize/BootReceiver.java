package edu.skku.inho.colorize;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * Created by In-Ho Han on 2/28/16.
 */
public class BootReceiver extends BroadcastReceiver {
	public BootReceiver() {
	}

	@Override
	public void onReceive(Context context, Intent intent) {
		if (LockScreenDataManager.getInstance(context).isLockScreenRunning()) {
			Intent i = new Intent(context, ColorGroupingService.class);
			context.startService(i);
		}
	}
}
