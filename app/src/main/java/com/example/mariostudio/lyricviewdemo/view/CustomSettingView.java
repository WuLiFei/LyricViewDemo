package com.example.mariostudio.lyricviewdemo.view;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.OvalShape;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.example.mariostudio.lyricviewdemo.R;
import com.example.mariostudio.lyricviewdemo.util.PreferenceUtil;

/**
 * Created by MarioStudio on 2016/8/1.
 */

public class CustomSettingView extends LinearLayout {

    private TextView mDismissBtn;
    private SeekBar mTextSizeSeek;
    private SeekBar mLineSpaceSeek;
    private LinearLayout mColorLayout;

    private OnColorItemChangeListener colorItemChangeListener;

    private int colors[] = {Color.parseColor("#4FC5C7"), Color.parseColor("#F55064"), Color.parseColor("#262A3B"), Color.parseColor("#F2572D"), Color.parseColor("#97EC71"), Color.parseColor("#0EA8E3"), Color.parseColor("#F29C9C"), Color.parseColor("#5B4947"), Color.parseColor("#EFC028"), Color.parseColor("#DE9DD6")};

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
        initAllViews();
    }

    private void initAllViews() {
        setOnTouchListener(new CustomSettingViewTouchListener());

        mDismissBtn = (TextView) findViewById(R.id.btn_dismiss);
        mTextSizeSeek = (SeekBar) findViewById(R.id.seek_text_size);
        mLineSpaceSeek = (SeekBar) findViewById(R.id.seek_line_space);
        mColorLayout = (LinearLayout) findViewById(R.id.layout_color);

        mTextSizeSeek.setProgress((int) ((PreferenceUtil.getInstance(getContext()).getFloat(PreferenceUtil.KEY_TEXT_SIZE, 15.0f) - 15.0f) * 100 / 3.0f));
        mLineSpaceSeek.setProgress((int) ((PreferenceUtil.getInstance(getContext()).getFloat(PreferenceUtil.KEY_TEXT_SIZE, 12.0f) - 12.0f) * 100 / 3.0f));
        initColorLayout();
    }

    private void initColorLayout() {
        float width = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 48, getContext().getResources().getDisplayMetrics());
        mColorLayout.removeAllViews();
        for(int i = 0, size = colors.length; i < size; i ++) {
            ShapeDrawable shapeDrawable = new ShapeDrawable(new OvalShape());
            shapeDrawable.setBounds(0, 0, (int) width, (int) width);
            shapeDrawable.getPaint().setColor(colors[i]);
            View view = new View(getContext());
            view.setBackgroundDrawable(shapeDrawable);
            view.setTag(colors[i]);
            view.setOnClickListener(new ColorItemClickListener());
            MarginLayoutParams layoutParams = new LayoutParams((int) width, (int) width);
            layoutParams.setMargins((int) (width / 8), 0, (int) (width / 8), 0);
            mColorLayout.addView(view, layoutParams);
        }
    }

    private class CustomSettingViewTouchListener implements OnTouchListener {

        @Override
        public boolean onTouch(View view, MotionEvent event) {
            return true;
        }
    }

    private class ColorItemClickListener implements OnClickListener {

        @Override
        public void onClick(View view) {
            if(colorItemChangeListener != null) {
                colorItemChangeListener.onColorChanged((Integer) view.getTag());
            }
        }
    }

    public void setOnTextSizeChangeListener(SeekBar.OnSeekBarChangeListener seekBarChangeListener) {
        mTextSizeSeek.setOnSeekBarChangeListener(seekBarChangeListener);
    }

    public void setOnLineSpaceChangeListener(SeekBar.OnSeekBarChangeListener seekBarChangeListener) {
        mLineSpaceSeek.setOnSeekBarChangeListener(seekBarChangeListener);
    }

    public void setOnDismissBtnClickListener(OnClickListener clickListener) {
        mDismissBtn.setOnClickListener(clickListener);
    }

    public void setOnColorItemChangeListener(OnColorItemChangeListener colorItemChangeListener) {
        this.colorItemChangeListener = colorItemChangeListener;
    }

    public interface OnColorItemChangeListener {
        public void onColorChanged(int color);
    }
}
