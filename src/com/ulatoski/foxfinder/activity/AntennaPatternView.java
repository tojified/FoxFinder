package com.ulatoski.foxfinder.activity;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Context;
import android.graphics.*;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

/**
 * Created by tojified on 3/8/14.
 */
public class AntennaPatternView extends View {

    Paint mPaint;
    Path mPath = new Path();
    float mLineWidth = 5.0f;

    public AntennaPatternView(Context context) {
        super(context);
        mPaint = createPaint();
    }

    public AntennaPatternView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mPaint = createPaint();
    }

    public AntennaPatternView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        mPaint = createPaint();
    }

    private Paint createPaint() {
        return new Paint() {
            {
                setColor(Color.WHITE);
                setStyle(Style.STROKE);
                setStrokeCap(Cap.ROUND);
                setStrokeWidth(mLineWidth);
                setAntiAlias(true);
            }
        };
    }

    public void selfDestruct(int time) {
        this.animate()
            .alpha(0)
            .setDuration(time)
            .setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    removeSelf();
                }
            });
    }

    private void removeSelf() {
        ViewGroup v = (ViewGroup) this.getParent();
        if (v != null) v.removeView(this);
    }

    @Override
    public void onDraw(Canvas canvas) {
        if (mPath != null) canvas.drawPath(mPath, mPaint);  //clear canvas
    }

    public void setAntennaPattern(float[] data) {  //data includes radius value between 0 and 1 for polar graph
        double interval = Math.toRadians(360) / data.length; //evenly spaced on polar grid
        int w = ( getWidth() - (int)mLineWidth ) / 2;
        int h = ( getHeight() - (int)mLineWidth ) / 2;
        int s = ( w < h ) ? w : h; //scale for smaller dimension

        mPath.reset();
        mPath.moveTo(getX(data[data.length-1] * s, interval * (data.length-1)) + w,
                     getY(data[data.length-1] * s, interval * (data.length-1)) + h);
        for (int i = 0; i < data.length; i++) {
            float x = getX(data[i] * s, interval * i);
            float y = getY(data[i] * s, interval * i);
            mPath.lineTo(x + w, y + h);
        }
        invalidate();
    }

    //r * cos(theta)
    private float getX(float radius, double radians) {
        return (float) (radius * Math.cos(radians));
    }

    //r * sin(theta)
    private float getY(float radius, double radians) {
        return (float) (radius * Math.sin(radians));
    }

}
