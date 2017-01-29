package com.buzz.anim;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.RadialGradient;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.OvalShape;
import android.graphics.drawable.shapes.RectShape;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.CycleInterpolator;
import android.widget.Toast;

import com.buzz.activity.R;
import com.buzz.utils.GlobalConst;
import com.buzz.utils.ImageHelper;
import com.nineoldandroids.animation.Animator;
import com.nineoldandroids.animation.AnimatorListenerAdapter;
import com.nineoldandroids.animation.AnimatorSet;
import com.nineoldandroids.animation.Keyframe;
import com.nineoldandroids.animation.ObjectAnimator;
import com.nineoldandroids.animation.PropertyValuesHolder;
import com.nineoldandroids.animation.ValueAnimator;
import com.nineoldandroids.view.ViewPropertyAnimator;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.buzz.models.XYC;

/**
 * Created by NickChung on 5/8/15.
 */
@Deprecated
public class MyAnimationView extends View implements ValueAnimator.AnimatorUpdateListener {

    private static final float BALL_SIZE = 100f;

    private final ArrayList<ShapeHolder> balls = new ArrayList<ShapeHolder>();
    AnimatorSet animation = null;
    Animator bounceAnim = null;
    ShapeHolder ball = null;

    public MyAnimationView(Context context) {
        super(context);

        //addBall(0, 0, 0xff282828);
        //addBall(50, 50, 0xffFE9375);
        //addBall(150, 150, 0xff1DD5C0);
        //addBall(250, 250, 0xff00A8A7);
        //addBall(350, 350, 0xff698686);
    }

    public void createAnimation(List<XYC> xycBallList) {
        if (bounceAnim == null) {
            //just for AnimatorSet.playTogether(),without it can't work
            ShapeHolder ball = addBall(500, 500, 0xff282828, (int) BALL_SIZE, "TAG");
            ObjectAnimator yBouncer = ObjectAnimator.ofFloat(ball, "y",
                    ball.getY(), getHeight() - BALL_SIZE).setDuration(GlobalConst.ANIMATION_DURATION);
            yBouncer.setInterpolator(new CycleInterpolator(2));
            yBouncer.addUpdateListener(this);


            List<Animator> animatorList = new ArrayList<Animator>();
            animatorList.add(yBouncer);
            for (XYC xyc : xycBallList) {
                animatorList.add(getBallAnimator(xyc.X, xyc.Y, xyc.C, xyc.S, xyc.T));
            }

            bounceAnim = new AnimatorSet();
            ((AnimatorSet) bounceAnim).playTogether(animatorList);
        }
    }

    private ObjectAnimator getBallAnimator(float x, float y, int hexColor, int size, String tag) {
        ball = addBall(x, y, hexColor, size, tag);
        PropertyValuesHolder pvhW = PropertyValuesHolder.ofFloat("width", ball.getWidth(),
                ball.getWidth() * 2f);
        PropertyValuesHolder pvhH = PropertyValuesHolder.ofFloat("height", ball.getHeight(),
                ball.getHeight() * 2f);
        PropertyValuesHolder pvTX = PropertyValuesHolder.ofFloat("x", ball.getX(),
                ball.getX() - size / 2f);
        PropertyValuesHolder pvTY = PropertyValuesHolder.ofFloat("y", ball.getY(),
                ball.getY() - size / 2f);
        PropertyValuesHolder pvhAlpha = PropertyValuesHolder.ofFloat("alpha", 1.0f, 0.1f);

        /*
        ObjectAnimator whxyaBouncer = ObjectAnimator.ofPropertyValuesHolder(ball, pvhW, pvhH,
                pvTX, pvTY, pvhAlpha).setDuration(GlobalConst.ANIMATION_DURATION / 2);*/

        ObjectAnimator whxyaBouncer = ObjectAnimator.ofPropertyValuesHolder(ball, pvhAlpha)
                .setDuration(GlobalConst.ANIMATION_DURATION / 2);

        whxyaBouncer.setRepeatCount(1);
        whxyaBouncer.setRepeatMode(ValueAnimator.REVERSE);
        whxyaBouncer.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                balls.remove(((ObjectAnimator) animation).getTarget());
            }
        });

        return whxyaBouncer;
    }

    public void startAnimation() {
        bounceAnim.start();
    }

    public void resetAnimation() {
        if (bounceAnim != null) {
            bounceAnim.end();
        }
        bounceAnim = null;
    }

    public ShapeHolder addBall(float x, float y, int hexColor, int size, String tag) {
        OvalShape circle = new OvalShape();
        circle.resize(size, size);
        ShapeDrawable drawable = new ShapeDrawable(circle);
        ShapeHolder shapeHolder = new ShapeHolder(drawable);
        shapeHolder.setX(x);
        shapeHolder.setY(y);
        shapeHolder.setTag(tag);

        int color = hexColor;
        Paint paint = drawable.getPaint();
        int darkColor = hexColor;
        RadialGradient gradient = new RadialGradient(37.5f, 12.5f,
                50f, color, darkColor, Shader.TileMode.CLAMP);
        paint.setShader(gradient);
        shapeHolder.setPaint(paint);

        balls.add(shapeHolder);
        return shapeHolder;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        for (int i = 0; i < balls.size(); ++i) {
            ShapeHolder shapeHolder = balls.get(i);

            Paint paint = shapeHolder.getPaint();
            Bitmap bmp = BitmapFactory.decodeResource(getResources(), R.drawable.ic_launcher);
            canvas.drawBitmap(bmp
                    , shapeHolder.getX()
                    , shapeHolder.getY()
                    , paint);

            paint.setTextSize(25);
            canvas.drawText(shapeHolder.getTag()
                    , shapeHolder.getX()
                    , shapeHolder.getY() + shapeHolder.getHeight() / 2
                    , paint);

            canvas.save();
            canvas.translate(shapeHolder.getX(), shapeHolder.getY());
            shapeHolder.getShape().draw(canvas);
            canvas.restore();
        }
    }

    public void onAnimationUpdate(ValueAnimator animation) {
        invalidate();
    }
}