package com.ulatoski.foxfinder.activity;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Context;
import android.graphics.*;
import android.view.View;
import android.view.ViewGroup;

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
        //noinspection ConstantConditions
        this.animate().alpha(0)
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

    public void setAntennaPattern(float[] data, double interval) {  //data includes radius value between 0 and 1 for polar graph
        if (data.length == 0) return;
        int w = ( getWidth() ) / 2;
        int h = ( getHeight() ) / 2;
        int s = ( w < h ) ? w - (int)mLineWidth : h - (int)mLineWidth; //scale for smaller dimension

        mPath.reset();
        mPath.moveTo(getX(data[0] * s, interval * 0) + w,
                     getY(data[0] * s, interval * 0) + h);
        for (int i = 1; i < data.length; i++) {
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
