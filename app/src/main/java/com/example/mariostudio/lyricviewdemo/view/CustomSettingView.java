package com.example.mariostudio.lyricviewdemo.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.LinearLayout;

import com.example.mariostudio.lyricviewdemo.R;

/**
 * Created by MarioStudio on 2016/8/1.
 */

public class CustomSettingView extends LinearLayout {

    public CustomSettingView(Context context) {
        super(context);
        initCustomSettingView();
    }

    public CustomSettingView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initCustomSettingView();
    }

    public CustomSettingView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initCustomSettingView();
    }

    private void initCustomSettingView() {
        setOrientation(LinearLayout.VERTICAL);
        LayoutInflater.from(getContext()).inflate(R.layout.view_setting, this, true);
    }
}
