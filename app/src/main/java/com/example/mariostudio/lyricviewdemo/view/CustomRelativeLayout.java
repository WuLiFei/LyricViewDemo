package com.example.mariostudio.lyricviewdemo.view;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;
import android.widget.RelativeLayout;

import com.nineoldandroids.view.ViewHelper;

/**
 * Created by MarioStudio on 2016/8/1.
 */

public class CustomRelativeLayout extends RelativeLayout {

    private float height;
    private boolean mShowing;

    public CustomRelativeLayout(Context context) {
        super(context);
        initCustomRelativeLayout(context);
    }

    public CustomRelativeLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        initCustomRelativeLayout(context);
    }

    public CustomRelativeLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initCustomRelativeLayout(context);
    }

    private void initCustomRelativeLayout(Context context) {
        height = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 240, context.getResources().getDisplayMetrics());
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        setOnTouchListener(new CustomRelativeLayoutTouchListener());
        doShow();
    }

    private class CustomRelativeLayoutTouchListener implements OnTouchListener {

        @Override
        public boolean onTouch(View view, MotionEvent event) {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                doDismiss();
                break;
            }
            return true;
        }
    }

    public void show() {
        doShow();
    }

    public void dismiss() {
        doDismiss();
    }

    private void doShow() {
        setVisibility(View.INVISIBLE);
        ValueAnimator animator01 = ValueAnimator.ofFloat(1.0f, 0.0f);
        animator01.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {

            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                doUpdate((Float) animation.getAnimatedValue());
            }
        });
        animator01.addListener(new AnimatorListenerAdapter() {

            @Override
            public void onAnimationStart(Animator animation) {
                super.onAnimationStart(animation);
                View child = getChildAt(0);
                ViewHelper.setTranslationY(child, height);
                setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                mShowing = false;
            }
        });
        ValueAnimator animator02 = ValueAnimator.ofObject(new ArgbEvaluator(), Color.TRANSPARENT, Color.parseColor("#A0000000"));
        animator02.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {

            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                setBackgroundColor((Integer) animation.getAnimatedValue());
            }
        });
        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.play(animator01).with(animator02);
        animatorSet.setDuration(360);
        animatorSet.start();
    }

    private void doDismiss() {
        ValueAnimator animator01 = ValueAnimator.ofFloat(0.0f, 1.0f);
        animator01.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {

            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                doUpdate((Float) animation.getAnimatedValue());
            }
        });
        animator01.addListener(new AnimatorListenerAdapter() {

            @Override
            public void onAnimationStart(Animator animation) {
                super.onAnimationStart(animation);
                View child = getChildAt(0);
                ViewHelper.setTranslationY(child, 0);
                setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                mShowing = false;
                setVisibility(View.GONE);
            }
        });
        ValueAnimator animator02 = ValueAnimator.ofObject(new ArgbEvaluator(), Color.parseColor("#A0000000"), Color.TRANSPARENT);
        animator02.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {

            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                setBackgroundColor((Integer) animation.getAnimatedValue());
            }
        });
        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.play(animator01).with(animator02);
        animatorSet.setDuration(360);
        animatorSet.start();
    }

    private void doUpdate(float value) {
        View child = getChildAt(0);
        ViewHelper.setTranslationY(child, value * height);
    }

    public boolean isShowing() {
        return mShowing;
    }
}
