package com.comic.seexian.detail;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.GridView;

public class AroundGridView extends GridView {

	public AroundGridView(Context context, AttributeSet attrs) {
		super(context, attrs);

		AroundGridAdapter adapter = new AroundGridAdapter(context);
		setAdapter(adapter);
	}
}
