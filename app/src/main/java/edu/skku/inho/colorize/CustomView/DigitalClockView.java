package edu.skku.inho.colorize.CustomView;

import android.content.Context;
import android.content.res.TypedArray;
import android.database.ContentObserver;
import android.os.Handler;
import android.os.SystemClock;
import android.provider.Settings;
import android.text.format.DateFormat;
import android.util.AttributeSet;
import android.widget.TextView;

import java.util.Calendar;

import edu.skku.inho.colorize.R;

/**
 * Created by XEiN on 2/20/16.
 */
public class DigitalClockView extends TextView {
	public static final int TIME = 0;
	public static final int DATE = 1;
	private final static String m12 = "h:mm";
	private final static String m24 = "k:mm";
	Calendar mCalendar;
	String mFormat;
	private FormatChangeObserver mFormatChangeObserver;
	private Runnable mTicker;
	private Handler mHandler;
	private boolean mTickerStopped = false;
	private int mTextFormat;

	public DigitalClockView(Context context, int textFormat) {
		super(context);
		mTextFormat = textFormat;
		initClock();
	}

	private void initClock() {
		if (mCalendar == null) {
			mCalendar = Calendar.getInstance();
		}

		mFormatChangeObserver = new FormatChangeObserver();
		getContext().getContentResolver().registerContentObserver(
				Settings.System.CONTENT_URI, true, mFormatChangeObserver);

		setFormat();
	}

	private void setFormat() {
		if (mTextFormat == DigitalClockView.TIME) {
			if (get24HourMode()) {
				mFormat = m24;
			} else {
				mFormat = m12;
			}
		} else {
			mFormat = "ccc, MMM dd, yyyy";
		}
	}

	/**
	 * Pulls 12/24 mode from system settings
	 */
	private boolean get24HourMode() {
		return android.text.format.DateFormat.is24HourFormat(getContext());
	}

	public DigitalClockView(Context context, AttributeSet attrs) {
		super(context, attrs);

		TypedArray a = context.getTheme().obtainStyledAttributes(
				attrs, R.styleable.DigitalClockView,
				0, 0);

		try {
			mTextFormat = a.getInteger(R.styleable.DigitalClockView_dateFormat, 0);
		} finally {
			a.recycle();
		}

		initClock();
	}

	@Override
	protected void onAttachedToWindow() {
		mTickerStopped = false;
		super.onAttachedToWindow();
		mHandler = new Handler();

		/**
		 * requests a tick on the next hard-second boundary
		 */
		mTicker = new Runnable() {
			public void run() {
				if (mTickerStopped) return;
				mCalendar.setTimeInMillis(System.currentTimeMillis());
				setText(DateFormat.format(mFormat, mCalendar));
				invalidate();
				long now = SystemClock.uptimeMillis();
				long next = now + (1000 - now % 1000);
				mHandler.postAtTime(mTicker, next);
			}
		};
		mTicker.run();
	}

	@Override
	protected void onDetachedFromWindow() {
		super.onDetachedFromWindow();
		mTickerStopped = true;
	}

	private class FormatChangeObserver extends ContentObserver {
		public FormatChangeObserver() {
			super(new Handler());
		}

		@Override
		public void onChange(boolean selfChange) {
			setFormat();
		}
	}
}
