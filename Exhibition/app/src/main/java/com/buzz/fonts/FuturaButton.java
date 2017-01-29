package com.buzz.fonts;

import android.content.Context;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.widget.Button;

/**
 * Created by buzz on 2015/5/11.
 */
public class FuturaButton extends Button {
    public FuturaButton(Context context) {
        super(context);
    }

    public FuturaButton(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public FuturaButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void setTypeface(Typeface tf, int style) {
        if (style == Typeface.BOLD) {
            super.setTypeface(Typeface.createFromAsset(getContext().getAssets(), "fonts/futura_bold.ttf"));
        } else {
            super.setTypeface(Typeface.createFromAsset(getContext().getAssets(), "fonts/futura.ttf"));
        }
    }
}