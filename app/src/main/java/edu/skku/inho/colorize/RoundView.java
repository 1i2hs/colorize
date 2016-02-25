package edu.skku.inho.colorize;

import android.content.Context;
import android.graphics.*;
import android.graphics.Color;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;
import android.widget.GridView;

/**
 * Created by XEiN on 1/26/16.
 */
public class RoundView extends View {

	private Paint paint;

	public RoundView(Context context, AttributeSet attrs) {
		super(context, attrs);
		paint = new Paint(Paint.ANTI_ALIAS_FLAG);
		paint.setColor(Color.TRANSPARENT);

		int size = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 90, getResources().getDisplayMetrics());
		GridView.LayoutParams layoutParams = new GridView.LayoutParams(size, size);
		setLayoutParams(layoutParams);
	}

	public int getColor() {
		return paint.getColor();
	}

	public void setColor(int color) {
		paint.setColor(color);
		invalidate();
	}

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		int height = getHeight();
		int width = getWidth();

		canvas.drawCircle(width / 2, height / 2, (int) (width * 0.375), paint);
	}
}
