package com.mslibs.widget;

import android.app.Activity;
import android.content.Context;
import android.util.AttributeSet;
import android.widget.LinearLayout;

import com.ms.R;

public class CSpannedTextView extends LinearLayout {

	public CSpannedTextView(Context context) {
		super(context);
	}

	public CSpannedTextView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public CSpannedTextView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();
		((Activity) getContext()).getLayoutInflater().inflate(R.layout.widget_spanned_textview,
				this);
	}
}
