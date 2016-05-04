package edu.skku.inho.colorize;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class BootReceiver extends BroadcastReceiver {
	public BootReceiver() {
	}

	@Override
	public void onReceive(Context context, Intent intent) {
		Intent i = new Intent(context, ColorGroupingService.class);
		context.startService(i);
	}
}
