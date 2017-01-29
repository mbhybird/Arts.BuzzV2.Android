package com.buzz.fonts;

import android.content.Context;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.widget.TextView;

/**
 * Created by buzz on 2015/5/11.
 */
public class FuturaTextView extends TextView {
    public FuturaTextView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public FuturaTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public FuturaTextView(Context context) {
        super(context);
    }

    public void setTypeface(Typeface tf, int style) {
        if (style == Typeface.BOLD) {
            super.setTypeface(Typeface.createFromAsset(getContext().getAssets(), "fonts/futura_bold.ttf"));
        } else {
            super.setTypeface(Typeface.createFromAsset(getContext().getAssets(), "fonts/futura.ttf"));
        }
    }

}
