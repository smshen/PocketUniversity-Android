package com.mslibs.widget;

import android.app.Activity;
import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.RelativeLayout;

public class CPagerItem extends RelativeLayout {
	public CPagerItem(Context context) {
		super(context);
	}

	public CPagerItem(Context context, AttributeSet attrs) {
		super(context, attrs);
		LayoutParams params = new LayoutParams(LayoutParams.MATCH_PARENT,
				LayoutParams.MATCH_PARENT);
		setLayoutParams(params);
	}

	public CPagerItem(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	public void setContentView(int resource) {
		View view = ((Activity) getContext()).getLayoutInflater().inflate(resource, null);
		LayoutParams params = new LayoutParams(LayoutParams.MATCH_PARENT,
				LayoutParams.MATCH_PARENT);
		view.setLayoutParams(params);
		addView(view);
	}
}
