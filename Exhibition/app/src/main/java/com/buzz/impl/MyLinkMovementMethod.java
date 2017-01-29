package com.buzz.impl;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.text.Layout;
import android.text.Selection;
import android.text.Spannable;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.URLSpan;
import android.view.MotionEvent;
import android.widget.TextView;

/**
 * Created by NickChung on 11/17/15.
 */
public class MyLinkMovementMethod extends LinkMovementMethod {
    private Context _context;
    public MyLinkMovementMethod(Context context) {
        _context = context;
    }
    @Override
    public boolean onTouchEvent(TextView widget, Spannable buffer, MotionEvent event) {
        int action = event.getAction();

        if (action == MotionEvent.ACTION_UP ||
                action == MotionEvent.ACTION_DOWN) {
            int x = (int) event.getX();
            int y = (int) event.getY();

            x -= widget.getTotalPaddingLeft();
            y -= widget.getTotalPaddingTop();

            x += widget.getScrollX();
            y += widget.getScrollY();

            Layout layout = widget.getLayout();
            int line = layout.getLineForVertical(y);
            int off = layout.getOffsetForHorizontal(line, x);

            ClickableSpan[] link = buffer.getSpans(off, off, ClickableSpan.class);

            if (link.length != 0) {
                if (action == MotionEvent.ACTION_UP) {
                    try {
                        //link[0].onClick(widget);//重写打开浏览器方法，在模态窗口下会出现异常;
                        Uri uri = Uri.parse(((URLSpan)link[0]).getURL());
                        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                        _context.startActivity(intent);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else if (action == MotionEvent.ACTION_DOWN) {
                    Selection.setSelection(buffer,
                            buffer.getSpanStart(link[0]),
                            buffer.getSpanEnd(link[0]));
                }

                return true;
            } else {
                Selection.removeSelection(buffer);
            }
        }

        return super.onTouchEvent(widget, buffer, event);
    }

    public static MyLinkMovementMethod getInstance(Context context){
        if(null == sInstance){
            sInstance = new MyLinkMovementMethod(context);
        }
        return sInstance;
    }

    private static MyLinkMovementMethod sInstance;
}