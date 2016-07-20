package com.example.mariostudio.lyricviewdemo.window;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.view.WindowManager;
import android.widget.PopupWindow;

/**
 * Created by WuLiFei on 2016/5/30.
 */

public class BasePopupWindow extends PopupWindow {

    private Context mContext;
    private boolean tweenAnimation;
    private boolean closable = true;
    private float mShowAlpha = 0.75f;
    private Drawable mBackgroundDrawable;

    public BasePopupWindow(Context context) {
        this(context, true);
    }

    public BasePopupWindow(Context context, boolean tweenAnimation) {
        this.tweenAnimation = tweenAnimation;
        this.mContext = context;
        initBasePopupWindow();
    }

    @Override
    public void setOutsideTouchable(boolean touchable) {
        super.setOutsideTouchable(touchable);
        setupClosable();
    }

    private void setupClosable() {
        if(isClosable() && isOutsideTouchable()) {
            if(mBackgroundDrawable == null) {
                mBackgroundDrawable = new ColorDrawable(0x00000000);
            }
            super.setBackgroundDrawable(mBackgroundDrawable);
        } else {
            super.setBackgroundDrawable(null);
        }
    }

    public void setClosable(boolean closable) {
        this.closable = closable;
        setupClosable();
    }

    public boolean isClosable() {
        return closable;
    }

    @Override
    public void setBackgroundDrawable(Drawable background) {
        mBackgroundDrawable = background;
        setOutsideTouchable(isOutsideTouchable());
    }

    /**
     * 初始化BasePopupWindow的一些信息
     * */
    private void initBasePopupWindow() {
        setAnimationStyle(android.R.style.Animation_Dialog);
        setHeight(ViewGroup.LayoutParams.WRAP_CONTENT);
        setWidth(ViewGroup.LayoutParams.WRAP_CONTENT);
        setOutsideTouchable(true);
        setFocusable(true);
    }

    @Override
    public void setContentView(View contentView) {
        if(contentView != null) {
            contentView.getViewTreeObserver().addOnGlobalFocusChangeListener(new focusChangeListener());
//            contentView.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
            super.setContentView(contentView);
            addKeyListener(contentView);
        }
    }

    public Context getContext() {
        return mContext;
    }

    @Override
    public void showAtLocation(View parent, int gravity, int x, int y) {
        super.showAtLocation(parent, gravity, x, y);
        if(tweenAnimation) {
            showAnimator().start();
        }
    }

    @Override
    public void showAsDropDown(View anchor) {
        super.showAsDropDown(anchor);
        if(tweenAnimation) {
            showAnimator().start();
        }
    }

    @Override
    public void showAsDropDown(View anchor, int xoff, int yoff) {
        super.showAsDropDown(anchor, xoff, yoff);
        if(tweenAnimation) {
            showAnimator().start();
        }
    }

    @Override
    public void showAsDropDown(View anchor, int xoff, int yoff, int gravity) {
        super.showAsDropDown(anchor, xoff, yoff, gravity);
        if(tweenAnimation) {
            showAnimator().start();
        }
    }

    @Override
    public void dismiss() {
        super.dismiss();
        if(tweenAnimation) {
            dismissAnimator().start();
        }
    }

    /**
     * 窗口显示，窗口背景透明度渐变动画
     * */
    private ValueAnimator showAnimator() {
        ValueAnimator animator = ValueAnimator.ofFloat(1.0f, mShowAlpha);
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {

            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float alpha = (float) animation.getAnimatedValue();
                setWindowBackgroundAlpha(alpha);
            }
        });

        animator.addListener(new AnimatorListenerAdapter() {

            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                setWindowBackgroundAlpha(mShowAlpha);
            }
        });
        animator.setDuration(360);
        return animator;
    }

    /**
     * 窗口隐藏，窗口背景透明度渐变动画
     * */
    private ValueAnimator dismissAnimator() {
        ValueAnimator animator = ValueAnimator.ofFloat(mShowAlpha, 1.0f);
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {

            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float alpha = (float) animation.getAnimatedValue();
                setWindowBackgroundAlpha(alpha);
            }
        });

        animator.addListener(new AnimatorListenerAdapter() {

            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                setWindowBackgroundAlpha(1.0f);
            }
        });
        animator.setDuration(320);
        return animator;
    }

    /**
     * 为窗体添加outside点击事件
     * */
    private void addKeyListener(View contentView) {
        if(contentView != null) {
            contentView.setFocusable(true);
            contentView.setFocusableInTouchMode(true);
            contentView.setOnKeyListener(new View.OnKeyListener() {

                @Override
                public boolean onKey(View view, int keyCode, KeyEvent event) {
                    switch (keyCode) {
                        case KeyEvent.KEYCODE_BACK:
                            if(closable) {
                                dismiss();
                            }
                            return true;
                        default:
                            break;
                    }
                    return false;
                }
            });
        }
    }

    /**
     * 控制窗口背景的不透明度
     * */
    private void setWindowBackgroundAlpha(float alpha) {
        Window window = ((Activity)getContext()).getWindow();
        WindowManager.LayoutParams layoutParams = window.getAttributes();
        layoutParams.alpha = alpha;
        window.setAttributes(layoutParams);
    }

    /**
     * 为视图中的View添加点击事件
     * */
    public void addOnClickCallBack(int resId, View.OnClickListener onClickListener) {
        View view = getContentView().findViewById(resId);
        if(view != null) {
            view.setOnClickListener(onClickListener);
        }
    }

    class focusChangeListener implements ViewTreeObserver.OnGlobalFocusChangeListener {

        @Override
        public void onGlobalFocusChanged(View oldFocus, View newFocus) {
            addKeyListener(newFocus);
        }
    }
}
