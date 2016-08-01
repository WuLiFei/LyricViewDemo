package com.example.mariostudio.lyricviewdemo.view;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.RelativeLayout;

import com.nineoldandroids.view.ViewHelper;

/**
 * Created by MarioStudio on 2016/8/1.
 */

public class CustomRelativeLayout extends RelativeLayout {

    public CustomRelativeLayout(Context context) {
        super(context);
    }

    public CustomRelativeLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public CustomRelativeLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void show() {
        doShow();
    }

    public void dismiss() {

    }

    private void doShow() {
        ValueAnimator animator = ValueAnimator.ofFloat(1.0f, 0.0f);
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {

            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                doUpdate((Float) animation.getAnimatedValue());
            }
        });

        animator.setDuration(360);
        animator.start();
    }

    private void doDismiss() {
        ValueAnimator animator = ValueAnimator.ofFloat(0.0f, 1.0f);
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {

            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                doUpdate((Float) animation.getAnimatedValue());
            }
        });
        animator.setDuration(360);
        animator.start();
    }

    private void doUpdate(float value) {
        View child = getChildAt(0);
        Log.e(getClass().getName(), "setTranslationY： " + value * child.getHeight());
        ViewHelper.setTranslationY(child, value * child.getHeight());
    }
}
