package edu.skku.inho.colorize;

import android.app.KeyguardManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;

import edu.skku.inho.colorize.LockScreenPage.MainActivity;

/**
 * Created by XEiN on 2/20/16.
 */
public class ScreenStateReceiver extends BroadcastReceiver {
	private KeyguardManager mKeyguardManager = null;
	private KeyguardManager.KeyguardLock mKeyLock = null;
	private TelephonyManager mTelephonyManager = null;
	private boolean mIsPhoneIdle = true;

	private PhoneStateListener phoneListener = new PhoneStateListener(){
		@Override
		public void onCallStateChanged(int state, String incomingNumber){
			switch(state){
				case TelephonyManager.CALL_STATE_IDLE :
					mIsPhoneIdle = true;
					break;
				case TelephonyManager.CALL_STATE_RINGING :
					mIsPhoneIdle = false;
					break;
				case TelephonyManager.CALL_STATE_OFFHOOK :
					mIsPhoneIdle = false;
					break;
			}
		}
	};

	@Override
	public void onReceive(Context context, Intent intent) {
		//SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);

		//if (intent.getAction().equals(Intent.ACTION_SCREEN_OFF) && sharedPreferences.getBoolean(Keys.IS_COLOR_DATA_READY, true)) {
		if (LockScreenDataProvider.getInstance(context).isColorDataReady()) {
			/*if (mKeyguardManager == null)
				mKeyguardManager = (KeyguardManager) context.getSystemService(Context.KEYGUARD_SERVICE);

			if (mKeyLock == null)
				mKeyLock = mKeyguardManager.newKeyguardLock(Context.KEYGUARD_SERVICE);
*/
			if(mTelephonyManager == null){
				mTelephonyManager = (TelephonyManager)context.getSystemService(Context.TELEPHONY_SERVICE);
				mTelephonyManager.listen(phoneListener, PhoneStateListener.LISTEN_CALL_STATE);
			}

			if(mIsPhoneIdle) {
				//disableKeyguard();

				Intent mainActivityIntent = new Intent(context, MainActivity.class);
				mainActivityIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				context.startActivity(mainActivityIntent);
			}
		}
	}

	public void disableKeyguard() {
		mKeyLock.disableKeyguard();
	}

	public void reenableKeyguard() {
		mKeyLock.reenableKeyguard();
	}
}
