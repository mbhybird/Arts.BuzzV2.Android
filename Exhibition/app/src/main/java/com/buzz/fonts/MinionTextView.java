package com.buzz.fonts;

import android.content.Context;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.widget.TextView;

/**
 * Created by buzz on 2015/5/11.
 */
public class MinionTextView  extends TextView {
    public MinionTextView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public MinionTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public MinionTextView(Context context) {
        super(context);
    }

    public void setTypeface(Typeface tf, int style) {
        if (style == Typeface.BOLD) {
            super.setTypeface(Typeface.createFromAsset(getContext().getAssets(), "fonts/minion_pro.otf"));
        } else {
            super.setTypeface(Typeface.createFromAsset(getContext().getAssets(), "fonts/minion_pro.otf"));
        }
    }
}
