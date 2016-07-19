package com.example.mariostudio.lyricviewdemo;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.animation.OvershootInterpolator;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by MarioStudio on 2016/7/16.
 */

public class LyricView extends View {

    private int mLineCount;
    private float mLineHeight;
    private float mScrollY = 0;
    private float mLineSpace = 0;
    private float mShaderWidth = 0;
    private int mCurrentShowLine = 0;
    private int mCurrentPlayLine = 0;

    private boolean mUserTouch = false;
    private boolean mPlayerShow = false;

    /***/
    private int mPlayerWidth = 0;
    private int maximumFlingVelocity;
    private Rect mPlayerBound, mTimerBound;
    private VelocityTracker mVelocityTracker;

    private LyricInfo mLyricInfo;
    private String def_time = "--:--";
    private String def_text = "点击搜索更多";
    private Paint mTextPaint, mLinePaint, mTimerPaint, mPlayerPaint;

    private OnPlayerClickListener mClickListener;

    private final int MSG_PLAYER_SLIDE = 0x158;
    private final int MSG_PLAYER_HIDE = 0x157;

    private ValueAnimator mFlingAnimator;
    private boolean mSliding = false;

    public LyricView(Context context) {
        super(context);
        initMyView(context);
    }

    public LyricView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initMyView(context);
    }

    public LyricView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initMyView(context);
    }

    private void initMyView(Context context) {
        maximumFlingVelocity = ViewConfiguration.get(context).getScaledMaximumFlingVelocity();
        float density = getResources().getDisplayMetrics().density;
        initAllPaints(density);
        initAllBounds(density);
    }

    private void initAllBounds(float density) {
        mLineSpace = 16 * density;

        mPlayerWidth = (int) (20 * density);

        mTimerBound = new Rect();
        mTimerPaint.getTextBounds(def_time, 0, def_time.length(), mTimerBound);

        Rect textBound = new Rect();
        mTextPaint.getTextBounds(def_text, 0, def_text.length(), textBound);
        mLineHeight = textBound.height() + mLineSpace;
    }

    private void initAllPaints(float density) {
        mTextPaint = new Paint();
        mTextPaint.setDither(true);
        mTextPaint.setAntiAlias(true);
        mTextPaint.setTextSize(14 * density);
        mTextPaint.setTextAlign(Paint.Align.CENTER);
        mTextPaint.setColor(Color.parseColor("#EFEFEF"));

        mLinePaint = new Paint();
        mLinePaint.setDither(true);
        mLinePaint.setAntiAlias(true);
        mLinePaint.setColor(Color.parseColor("#EFEFEF"));
        mLinePaint.setAlpha(36);

        mPlayerPaint = new Paint();
        mPlayerPaint.setDither(true);
        mPlayerPaint.setAntiAlias(true);
        mPlayerPaint.setStrokeWidth(2.0f);
        mPlayerPaint.setStyle(Paint.Style.STROKE);
        mPlayerPaint.setColor( Color.parseColor("#EFEFEF"));
        mPlayerPaint.setAlpha(114);

        mTimerPaint = new Paint();
        mTimerPaint.setDither(true);
        mTimerPaint.setAntiAlias(true);
        mTimerPaint.setTextSize(10 * density);
        mTimerPaint.setTextAlign(Paint.Align.CENTER);
        mTimerPaint.setColor( Color.parseColor("#EFEFEF"));
        mTimerPaint.setAlpha(114);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        mShaderWidth = getMeasuredHeight() * 0.3f;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if(mLyricInfo != null && mLyricInfo.song_lines != null && mLyricInfo.song_lines.size() > 0) {
            for(int i = 0, size = mLineCount; i < size; i ++) {
                float x = getMeasuredWidth() * 0.5f;
                float y = getMeasuredHeight() * 0.5f + (i + 0.5f) * mLineHeight - 6 - mLineSpace * 0.5f - mScrollY;
                if(y + mLineHeight * 0.5f < 0) {
                    continue;
                }
                if(y - mLineHeight * 0.5f > getMeasuredHeight()) {
                    break;
                }
                if(i == mCurrentPlayLine - 1) {
                    mTextPaint.setColor(Color.parseColor("#3CB371"));
                } else {
                    if(i == mCurrentShowLine - 1) {
                        mTextPaint.setColor(Color.parseColor("#969696"));
                    } else {
                        mTextPaint.setColor( Color.parseColor("#EFEFEF"));
                    }
                }
                if(y > getMeasuredHeight() - mShaderWidth || y < mShaderWidth) {
                    if(y < mShaderWidth) {
                        mTextPaint.setAlpha(26 + (int) (23000.0f * y / mShaderWidth * 0.01f));
                    } else {
                        mTextPaint.setAlpha(26 + (int) (23000.0f * (getMeasuredHeight() - y) / mShaderWidth * 0.01f));
                    }
                } else {
                    mTextPaint.setAlpha(255);
                }
                canvas.drawText(mLyricInfo.song_lines.get(i).content, x, y, mTextPaint);
            }
        } else {
            mTextPaint.setColor(Color.parseColor("#3CB371"));
            canvas.drawText(def_text, getMeasuredWidth() * 0.5f, (getMeasuredHeight() + mLineHeight - 6) * 0.5f, mTextPaint);
        }
        /**
         * 滑动提示部分内容绘制
         * */
        if(mPlayerShow && scrollable()) {
            drawPlayer(canvas);
            canvas.drawLine(mPlayerBound.right + 12 , getMeasuredHeight() * 0.5f, getMeasuredWidth() - mTimerBound.width() - mTimerBound.width() - 12, getMeasuredHeight() * 0.5f, mLinePaint);
            canvas.drawText(measureCurrentTime(), getMeasuredWidth() - mTimerBound.width() - 6, (getMeasuredHeight() + mTimerBound.height() - 6) * 0.5f, mTimerPaint);
        }
    }

    /**
     * 获取当前滑动到的位置的当前时间
     * */
    private String measureCurrentTime() {
        DecimalFormat format = new DecimalFormat("00");
        if(mLyricInfo != null && mLineCount > 0 && mCurrentShowLine - 1 < mLineCount && mCurrentShowLine > 0) {
            return format.format(mLyricInfo.song_lines.get(mCurrentShowLine - 1).start / 1000 / 60) + ":" + format.format(mLyricInfo.song_lines.get(mCurrentShowLine - 1).start / 1000 % 60);
        }
        if(mLyricInfo != null && mLineCount > 0 && (mCurrentShowLine - 1) >= mLineCount) {
            return format.format(mLyricInfo.song_lines.get(mLineCount - 1).start / 1000 / 60) + ":" + format.format(mLyricInfo.song_lines.get(mLineCount - 1).start / 1000 % 60);
        }
        if(mLyricInfo != null && mLineCount > 0 && mCurrentShowLine - 1 <= 0) {
            return format.format(mLyricInfo.song_lines.get(0).start / 1000 / 60) + ":" + format.format(mLyricInfo.song_lines.get(0).start / 1000 % 60);
        }
        return def_time;
    }

    /**
     * 绘制左侧的播放按钮
     * */
    private void drawPlayer(Canvas canvas) {
        mPlayerBound = new Rect(12, (int) (getMeasuredHeight() * 0.5f - mPlayerWidth * 0.5f), mPlayerWidth + 12, (int) (getMeasuredHeight() * 0.5f + mPlayerWidth * 0.5f));

        Path path = new Path();
        float radio = mPlayerBound.width() * 0.3f;
        float value = (float) Math.sqrt(Math.pow(radio, 2) - Math.pow(radio * 0.5f, 2));
        path.moveTo(mPlayerBound.centerX() - radio * 0.5f, mPlayerBound.centerY() - value);
        path.lineTo(mPlayerBound.centerX() - radio * 0.5f, mPlayerBound.centerY() + value);
        path.lineTo(mPlayerBound.centerX() + radio, mPlayerBound.centerY());
        path.lineTo(mPlayerBound.centerX() - radio * 0.5f, mPlayerBound.centerY() - value);
        canvas.drawPath(path, mPlayerPaint);

        canvas.drawCircle(mPlayerBound.centerX(), mPlayerBound.centerY(), mPlayerBound. width() * 0.48f, mPlayerPaint);
    }

    private float mDownX, mDownY, mLastScrollY;      // 记录手指按下时的坐标和当前的滑动偏移量

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if(mVelocityTracker == null) {
            mVelocityTracker = VelocityTracker.obtain();
        }
        mVelocityTracker.addMovement(event);
        switch (event.getAction()) {
            case MotionEvent.ACTION_CANCEL:
                actionCancel(event);
                break;
            case MotionEvent.ACTION_DOWN:
                actionDown(event);
                break;
            case MotionEvent.ACTION_MOVE:
                actionMove(event);
                break;
            case MotionEvent.ACTION_UP:
                actionUp(event);
                break;
            default:
                break;
        }
        invalidateView();
        return true;
    }

    /**
     * 刷新View
     * */
    private void invalidateView() {
        if(Looper.getMainLooper() == Looper.myLooper()) {
            invalidate();
        } else {
            postInvalidate();
        }
    }


    private void setUserTouch(boolean isUserTouch) {
        if(mUserTouch == isUserTouch) {
            return;
        }
        mUserTouch = isUserTouch;
        if(isUserTouch) {
            mPlayerShow = isUserTouch;
        }
    }

    private void releaseVelocityTracker() {
        if(null != mVelocityTracker) {
            mVelocityTracker.clear();
            mVelocityTracker.recycle();
            mVelocityTracker = null;
        }
    }

    /**
     * 手势取消执行事件
     * */
    private void actionCancel(MotionEvent event) {
        releaseVelocityTracker();
    }

    /**
     * 手势按下执行事件
     * */
    private void actionDown(MotionEvent event) {
        postman.removeMessages(MSG_PLAYER_SLIDE);
        postman.removeMessages(MSG_PLAYER_HIDE);
        mLastScrollY = mScrollY;
        mDownX = event.getX();
        mDownY = event.getY();
        setUserTouch(true);
    }

    /**
     * 手势移动执行事件
     * */
    private void actionMove(MotionEvent event) {
        if(scrollable()) {
            final VelocityTracker tracker = mVelocityTracker;
            tracker.computeCurrentVelocity(1000, maximumFlingVelocity);
            float scrollY = mLastScrollY + mDownY - event.getY();   // 102  -2  58  42
            float value01 = scrollY - (mLineCount * mLineHeight * 0.5f);   // 52  -52  8  -8
            float value02 = ((Math.abs(value01) - (mLineCount * mLineHeight * 0.5f)));   // 2  2  -42  -42
            mScrollY = value02 > 0 ? scrollY - (value02 * 0.5f * value01 / Math.abs(value01)) : scrollY;
            measureCurrentLine();
        }
    }

    /**
     * 手势抬起执行事件
     * */
    private void actionUp(MotionEvent event) {
        releaseVelocityTracker();
        postman.sendEmptyMessageDelayed(MSG_PLAYER_HIDE, 2400);
        if(scrollable()) {
            setUserTouch(false);
            if(overScrolled() && mScrollY < 0) {
                smoothScrollTo(0);
                return;
            }
            if(overScrolled() && mScrollY > mLineHeight * ( mLineCount - 1 )) {
                smoothScrollTo(mLineHeight * (mLineCount - 1));
                return;
            }
            if(mPlayerShow && clickPlayer(event)) {
                if(mCurrentShowLine != mCurrentPlayLine) {
                    mCurrentPlayLine = mCurrentShowLine;
                    mScrollY = measureCurrentScrollY(mCurrentShowLine);
                    if(mClickListener != null) {
                        mClickListener.onPlayerClicked(mLyricInfo.song_lines.get(mCurrentPlayLine - 1).start);
                    }
                }
            }
        }
    }

    /**
     * 通过偏移量获得当前显示的行数
     * */
    private void measureCurrentLine() {
        float baseScrollY = mScrollY + mLineHeight * 0.5f;
        mCurrentShowLine = (int) (baseScrollY / mLineHeight + 1);
    }

    /**
     * 通过行数计算当前歌词内容的偏移量
     * */
    private float measureCurrentScrollY(int line) {
        return (line - 1) * mLineHeight;
    }

    /**
     * 判断当前点击事件是否落在播放按钮触摸区域范围内
     * */
    private boolean clickPlayer(MotionEvent event) {
        return mPlayerBound != null && event.getX() > mPlayerBound.left && event.getX() < mPlayerBound.right && event.getY() > mPlayerBound.top && event.getY() < mPlayerBound.bottom;
    }

    /**
     * 从当前位置滑动到指定位置上
     * */
    private void smoothScrollTo(float toY) {
        final ValueAnimator animator = ValueAnimator.ofFloat(mScrollY , toY);
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {

            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                if(mUserTouch) {
                    animator.cancel();
                    return;
                }
                mScrollY = (float) animation.getAnimatedValue();
                invalidateView();
            }
        });

        animator.addListener(new AnimatorListenerAdapter() {

            @Override
            public void onAnimationStart(Animator animation) {
                super.onAnimationStart(animation);
                mSliding = true;
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                mSliding = false;
                measureCurrentLine();
                invalidateView();
            }
        });
        animator.setDuration(640);
        animator.setInterpolator(new OvershootInterpolator(0.5f));

        animator.start();
    }

    /**
     * 判断是否可以进行滑动
     * */
    private boolean scrollable() {
        return mLyricInfo != null && mLyricInfo.song_lines != null && mLyricInfo.song_lines.size() > 0;
    }

    /**
     * 判断当前View是否已经滑动到歌词的内容区域之外
     * */
    private boolean overScrolled() {
        return scrollable() && (mScrollY > mLineHeight * (mLineCount - 1) || mScrollY < 0);
    }

    Handler postman = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case MSG_PLAYER_HIDE:
                    postman.sendEmptyMessageDelayed(MSG_PLAYER_SLIDE, 1200);
                    mPlayerShow = false;
                    invalidateView();
                case MSG_PLAYER_SLIDE:
                    smoothScrollTo(measureCurrentScrollY(mCurrentPlayLine));
                    invalidateView();
            }
        }
    };

    /**
     * 根据当前给定的时间戳滑动到指定位置
     * */
    private void scrollToCurrent(long time) {
        int position = 0;
        if(scrollable()) {
            for(int i = 0, size = mLineCount; i < size; i ++) {
                LineInfo lineInfo = mLyricInfo.song_lines.get(i);
                if(lineInfo != null && lineInfo.start >= time) {
                    position = i;
                    break;
                }
                if(i == mLineCount - 1) {
                    position = mLineCount;
                }
            }
        }
        if(mCurrentPlayLine != position && !mUserTouch && !mSliding) {
            smoothScrollTo(measureCurrentScrollY(position));
        }
        mCurrentPlayLine = position;
    }

    /**
     *
     * */
    private void setupLyricResource(InputStream inputStream) {
        if(inputStream != null) {
            try {
                LyricInfo lyricInfo = new LyricInfo();
                lyricInfo.song_lines = new ArrayList<>();
                InputStreamReader inputStreamReader = new InputStreamReader(inputStream, "GBK");
                BufferedReader reader = new BufferedReader(inputStreamReader);
                String line = null;
                while((line = reader.readLine()) != null) {
                    analyzeLyric(lyricInfo, line);
                }
                reader.close();
                inputStream.close();
                inputStreamReader.close();

                mLyricInfo = lyricInfo;
                mLineCount = mLyricInfo.song_lines.size();
                invalidateView();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 逐行解析歌词内容
     * */
    private void analyzeLyric(LyricInfo lyricInfo, String line) {
        int index = line.lastIndexOf("]");
        if(line != null && line.startsWith("[offset:")) {
            // 时间偏移量
            String string = line.substring(8, index).trim();
            lyricInfo.song_offset = Long.parseLong(string);
            return;
        }
        if(line != null && line.startsWith("[ti:")) {
            // title 标题
            String string = line.substring(4, index).trim();
            lyricInfo.song_title = string;
            return;
        }
        if(line != null && line.startsWith("[ar:")) {
            // artist 作者
            String string = line.substring(4, index).trim();
            lyricInfo.song_artist = string;
            return;
        }
        if(line != null && line.startsWith("[al:")) {
            // album 所属专辑
            String string = line.substring(4, index).trim();
            lyricInfo.song_album = string;
            return;
        }
        if(line != null && line.startsWith("[by:")) {
            return;
        }
        if(line != null && index == 9 && line.trim().length() > 10) {
            // 歌词内容
            LineInfo lineInfo = new LineInfo();
            lineInfo.content = line.substring(10, line.length());
            lineInfo.start = measureStartTimeMillis(line.substring(0, 10));
            lyricInfo.song_lines.add(lineInfo);
        }
    }

    /**
     * 从字符串中获得时间值
     * */
    private long measureStartTimeMillis(String str) {
        long minute = Long.parseLong(str.substring(1, 3));
        long second = Long.parseLong(str.substring(4, 6));
        long millisecond = Long.parseLong(str.substring(7, 9));
        return millisecond + second * 1000 + minute * 60 * 1000;
    }

    private void resetLyricInfo() {
        if(mLyricInfo != null) {
            if(mLyricInfo.song_lines != null) {
                mLyricInfo.song_lines.clear();
                mLyricInfo.song_lines = null;
            }
            mLyricInfo = null;
        }
    }



    class LyricInfo {
        List<LineInfo> song_lines;

        String song_artist;
        String song_title;
        String song_album;

        long song_offset;

    }

    class LineInfo {
        String content;
        long start;
    }

    /**
     * ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------ *
     *                                                                                             对外API                                                                                        *
     * ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------ *
     * */

    public void setCurrent(long current) {
        scrollToCurrent(current);
    }

    public void setLyricFile(File file) {
        try {
            setupLyricResource(new FileInputStream(file));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    public void setOnPlayerClickListener(OnPlayerClickListener mClickListener) {
        this.mClickListener = mClickListener;
    }

    public void reset(String message) {
        mCurrentPlayLine = mCurrentShowLine = 0;
        def_text = message;
        resetLyricInfo();
        mLineCount = 0;
        mScrollY = 0;
        invalidateView();
    }
}
