package com.comic.seexian.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.ImageButton;

import com.comic.seexian.Loge;
import com.comic.seexian.R;

public class ShrinkImageButton extends ImageButton {

	final static int SHRINK_SIZE = 30;

	Bitmap origin = null, fit = null, shrink = null;

	int state = MotionEvent.ACTION_UP;

	int shrinkSide = 0;

	public ShrinkImageButton(Context context, AttributeSet attrs) {
		super(context, attrs);

		TypedArray array = context.obtainStyledAttributes(attrs,
				R.styleable.ShrinkImageButtonView);

		shrinkSide = array.getInteger(
				R.styleable.ShrinkImageButtonView_shinkSide, 0);

		int imageID = attrs.getAttributeResourceValue(
				"http://schemas.android.com/apk/res/android", "src", -1);

		origin = ((BitmapDrawable) context.getResources().getDrawable(imageID))
				.getBitmap();

		Loge.i("shrinkSide = " + shrinkSide);
	}

	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		super.onSizeChanged(w, h, oldw, oldh);
		fit = Bitmap.createScaledBitmap(origin, w, h, true);
		shrink = Bitmap.createScaledBitmap(origin, w - SHRINK_SIZE, h
				- SHRINK_SIZE, true);
	}
	@Override
	protected void onDraw(Canvas canvas) {
		switch (state) {
			case MotionEvent.ACTION_DOWN :
				switch (shrinkSide) {
					case 0 :
						canvas.drawBitmap(shrink, SHRINK_SIZE, SHRINK_SIZE,
								null);
						break;
					case 1 :
						canvas.drawBitmap(shrink, 0, SHRINK_SIZE, null);
						break;
					case 2 :
						canvas.drawBitmap(shrink, SHRINK_SIZE, 0, null);
						break;
					case 3 :
						canvas.drawBitmap(shrink, 0, 0, null);
						break;
					case 4 :
						canvas.drawBitmap(shrink, SHRINK_SIZE / 2,
								SHRINK_SIZE / 2, null);
						break;
					default :
						break;
				}
				break;
			case MotionEvent.ACTION_UP :
				canvas.drawBitmap(fit, 0, 0, null);
				break;
			default :
				break;
		}

	}
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		int eventCode = event.getAction();
		switch (eventCode) {
			case MotionEvent.ACTION_DOWN :
				state = MotionEvent.ACTION_DOWN;
				invalidate();
				break;
			case MotionEvent.ACTION_UP :
				state = MotionEvent.ACTION_UP;
				invalidate();
				break;
			default :
				break;
		}
		return super.onTouchEvent(event);
	}

}
