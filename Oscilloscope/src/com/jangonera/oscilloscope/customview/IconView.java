package com.jangonera.oscilloscope.customview;

import android.content.Context;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.widget.TextView;

public class IconView extends TextView {

	public IconView(Context context) {
		super(context);
		init();
	}

	public IconView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	public IconView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init();
	}
    private void init(){
    	Typeface tf = Typeface.createFromAsset(getContext().getAssets(), "fonts/entypo.ttf");
        setTypeface(tf);
    }
}
