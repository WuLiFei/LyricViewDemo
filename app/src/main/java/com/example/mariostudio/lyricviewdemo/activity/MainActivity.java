package com.example.mariostudio.lyricviewdemo.activity;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.annotation.TargetApi;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.ViewStub;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.example.mariostudio.lyricviewdemo.Constant;
import com.example.mariostudio.lyricviewdemo.LyricView;
import com.example.mariostudio.lyricviewdemo.R;
import com.example.mariostudio.lyricviewdemo.util.PreferenceUtil;
import com.example.mariostudio.lyricviewdemo.view.CustomRelativeLayout;
import com.example.mariostudio.lyricviewdemo.view.CustomSettingView;
import com.lidroid.xutils.HttpUtils;
import com.lidroid.xutils.exception.HttpException;
import com.lidroid.xutils.http.ResponseInfo;
import com.lidroid.xutils.http.callback.RequestCallBack;
import com.nineoldandroids.view.ViewHelper;

import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, MediaPlayer.OnBufferingUpdateListener, MediaPlayer.OnPreparedListener, MediaPlayer.OnCompletionListener, SeekBar.OnSeekBarChangeListener, LyricView.OnPlayerClickListener {

    private LyricView lyricView;
    private MediaPlayer mediaPlayer;

    private View statueBar;
    private SeekBar display_seek;
    private TextView display_total;
    private TextView display_title;
    private TextView display_position;

    private ImageView btnPre, btnPlay, btnNext, btnSetting;

    private String song_urls[] = null;
    private String song_names[] = null;
    private String song_lyrics[] = null;

    private int position = 0;
    private State currentState = State.STATE_STOP;
    private ValueAnimator press_animator, up_animator;

    private ViewStub setting_layout;
    private CustomSettingView customSettingView;
    private CustomRelativeLayout customRelativeLayout;

    private final int MSG_REFRESH = 0x167;
    private final int MSG_LOADING = 0x177;
    private final int MSG_LYRIC_SHOW = 0x187;

    private long animatorDuration = 120;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            setTranslucentStatus();
        }

        initAllViews();
        initAllDatum();
    }

    @TargetApi(19)
    private void setTranslucentStatus() {
        Window window = getWindow();
        WindowManager.LayoutParams params = window.getAttributes();
        final int status = WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS;
        params.flags |= status;
        window.setAttributes(params);
    }

    public int getStatusBarHeight() {
        int result = 0;
        int resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            result = getResources().getDimensionPixelSize(resourceId);
        }
        return result;
    }

    private void initAllViews() {
        statueBar = findViewById(R.id.statue_bar);
        statueBar.getLayoutParams().height = getStatusBarHeight();
        display_title = (TextView) findViewById(R.id.title_view);
        display_position = (TextView) findViewById(android.R.id.text1);
        display_total = (TextView) findViewById(android.R.id.text2);
        display_seek = (SeekBar) findViewById(android.R.id.progress);
        display_seek.setOnSeekBarChangeListener(this);
        btnNext = (ImageView) findViewById(android.R.id.button3);
        btnPlay = (ImageView) findViewById(android.R.id.button2);
        btnPre = (ImageView) findViewById(android.R.id.button1);
        btnSetting = (ImageView) findViewById(R.id.action_setting);
        btnSetting.setOnClickListener(this);
        btnPlay.setOnClickListener(this);
        btnNext.setOnClickListener(this);
        btnPre.setOnClickListener(this);
        lyricView = (LyricView) findViewById(R.id.lyric_view);
        lyricView.setOnPlayerClickListener(this);
        lyricView.setLineSpace(PreferenceUtil.getInstance(MainActivity.this).getFloat(PreferenceUtil.KEY_TEXT_SIZE, 12.0f));
        lyricView.setTextSize(PreferenceUtil.getInstance(MainActivity.this).getFloat(PreferenceUtil.KEY_TEXT_SIZE, 15.0f));
        lyricView.setHighLightTextColor(PreferenceUtil.getInstance(MainActivity.this).getInt(PreferenceUtil.KEY_HIGHLIGHT_COLOR, Color.parseColor("#4FC5C7")));

        setting_layout = (ViewStub) findViewById(R.id.main_setting_layout);
    }

    private void initAllDatum() {
        song_lyrics = getResources().getStringArray(R.array.song_lyrics);
        song_names = getResources().getStringArray(R.array.song_names);
        song_urls = getResources().getStringArray(R.array.song_urls);

        mediaPlayerSetup();  // 准备
    }

    /**
     * 准备
     * */
    private void mediaPlayerSetup() {
        display_title.setText(song_names[position]);
        handler.removeMessages(MSG_LYRIC_SHOW);
        handler.sendEmptyMessageDelayed(MSG_LYRIC_SHOW, 420);
    }

    /**
     * 停止
     * */
    private void stop() {
        if(null != mediaPlayer) {
            mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer = null;
        }
        handler.removeMessages(MSG_REFRESH);
        lyricView.reset("载入歌词ing...");
        setCurrentState(State.STATE_STOP);
    }

    /**
     * 暂停
     * */
    private void pause() {
        if(mediaPlayer != null && currentState == State.STATE_PLAYING) {
            setCurrentState(State.STATE_PAUSE);
            mediaPlayer.pause();
            handler.removeMessages(MSG_REFRESH);
        }
    }

    /**
     * 开始
     * */
    private void start() {
        if(mediaPlayer != null && (currentState == State.STATE_PAUSE || currentState == State.STATE_PREPARE)) {
            setCurrentState(State.STATE_PLAYING);
            mediaPlayer.start();
            handler.sendEmptyMessage(MSG_REFRESH);
        }
    }

    /**
     * 上一首
     * */
    private void previous() {
        stop();
        position --;
        if(position < 0) {
            position = Math.min(Math.min(song_names.length, song_lyrics.length), song_urls.length) - 1;
        }
        mediaPlayerSetup();
    }

    /**
     * 上一首
     * */
    private void next() {
        stop();
        position ++;
        if(position >= Math.min(Math.min(song_names.length, song_lyrics.length), song_urls.length)) {
            position = 0;
        }
        mediaPlayerSetup();
    }

    @Override
    public void onPrepared(MediaPlayer mediaPlayer) {
        setCurrentState(State.STATE_PREPARE);
        DecimalFormat format = new DecimalFormat("00");
        display_seek.setMax(mediaPlayer.getDuration());
        display_total.setText(format.format(mediaPlayer.getDuration() / 1000 / 60) + ":" + format.format(mediaPlayer.getDuration() / 1000 % 60));
        start();
    }

    @Override
    public void onBufferingUpdate(MediaPlayer mediaPlayer, int percent) {
        display_seek.setSecondaryProgress((int) (mediaPlayer.getDuration() * 1.00f * percent / 100.0f));
    }

    @Override
    public void onCompletion(MediaPlayer mediaPlayer) {
        next();
    }

    /**
     * 设置当前播放状态
     * */
    private void setCurrentState(State state) {
        if(state == this.currentState) {
            return;
        }
        this.currentState = state;
        switch (state) {
            case STATE_PAUSE:
                btnPlay.setImageResource(R.mipmap.m_icon_player_play_normal);
                break;
            case STATE_PLAYING:
                btnPlay.setImageResource(R.mipmap.m_icon_player_pause_normal);
                break;
            case STATE_PREPARE:
                if(lyricView != null) {
                    lyricView.setPlayable(true);
                }
                setLoading(false);
                break;
            case STATE_STOP:
                if(lyricView != null) {
                    lyricView.setPlayable(false);
                }
                display_position.setText("--:--");
                display_seek.setSecondaryProgress(0);
                display_seek.setProgress(0);
                display_seek.setMax(100);
                btnPlay.setImageResource(R.mipmap.m_icon_player_play_normal);
                setLoading(false);
                break;
            case STATE_SETUP:
                File file = new File(Constant.lyricPath + song_names[position] + ".lrc");
                if(file.exists()) {
                    lyricView.setLyricFile(file, "GBK");
                } else {
                    downloadLyric(song_lyrics[position], file);
                }
                btnPlay.setImageResource(R.mipmap.m_icon_player_play_normal);
                setLoading(true);
                break;
            default:
                break;
        }
    }

    Handler handler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case MSG_REFRESH:
                    if(mediaPlayer != null) {
                        if(!display_seek.isPressed()) {
                            lyricView.setCurrentTimeMillis(mediaPlayer.getCurrentPosition());
                            DecimalFormat format = new DecimalFormat("00");
                            display_seek.setProgress(mediaPlayer.getCurrentPosition());
                            display_position.setText(format.format(mediaPlayer.getCurrentPosition() / 1000 / 60) + ":" + format.format(mediaPlayer.getCurrentPosition() / 1000 % 60));
                        }
                    }
                    handler.sendEmptyMessageDelayed(MSG_REFRESH, 120);
                    break;
                case MSG_LYRIC_SHOW:
                    try {
                        setCurrentState(State.STATE_SETUP);
                        mediaPlayer = new MediaPlayer();
                        mediaPlayer.setOnPreparedListener(MainActivity.this);
                        mediaPlayer.setOnCompletionListener(MainActivity.this);
                        mediaPlayer.setOnBufferingUpdateListener(MainActivity.this);
                        mediaPlayer.setDataSource(song_urls[position]);
                        mediaPlayer.prepareAsync();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    break;
                case MSG_LOADING:
                    Drawable background = btnPlay.getBackground();
                    int level = background.getLevel();
                    level = level + 300;
                    if(level > 10000) {
                        level = level - 10000;
                    }
                    background.setLevel(level);
                    handler.sendEmptyMessageDelayed(MSG_LOADING, 50);
                    break;
                default:
                    break;
            }
        }
    };

    private boolean mLoading = false;

    private void setLoading(boolean loading) {
        if(loading && !mLoading) {
            btnPlay.setBackgroundResource(R.drawable.rotate_player_loading);
            handler.sendEmptyMessageDelayed(MSG_LOADING, 200);
            mLoading = true;
            return;
        }
        if (!loading && mLoading) {
            handler.removeMessages(MSG_LOADING);
            btnPlay.setBackgroundColor(Color.TRANSPARENT);
            mLoading = false;
            return;
        }
    }

    @Override
    public void onPlayerClicked(long progress, String content) {
        if(mediaPlayer != null && (currentState == State.STATE_PLAYING || currentState == State.STATE_PAUSE)) {
            mediaPlayer.seekTo((int) progress);
            if(currentState == State.STATE_PAUSE) {
                start();
            }
        }
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        if(fromUser) {
            DecimalFormat format = new DecimalFormat("00");
            display_position.setText(format.format(progress / 1000 / 60) + ":" + format.format(progress / 1000 % 60));
        }
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
        handler.removeMessages(MSG_REFRESH);
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        mediaPlayer.seekTo(seekBar.getProgress());
        handler.sendEmptyMessageDelayed(MSG_REFRESH, 120);
    }

    private void downloadLyric(String url, File file) {
        HttpUtils httpUtils = new HttpUtils();
        httpUtils.download(url, file.getAbsolutePath(), true, true, new RequestCallBack<File>() {

            @Override
            public void onSuccess(ResponseInfo<File> responseInfo) {
                lyricView.setLyricFile(responseInfo.result, "GBK");
            }

            @Override
            public void onFailure(HttpException e, String s) {
                lyricView.setLyricFile(null, null);
            }
        });
    }

    @Override
    public void onClick(View view) {
        if(press_animator != null && press_animator.isRunning()) {
            press_animator.cancel();
        }
        if(up_animator != null && up_animator.isRunning()) {
            up_animator.cancel();
        }
        switch (view.getId()) {
            case android.R.id.button1:
                previous();
                break;
            case android.R.id.button2:
                if(currentState == State.STATE_PAUSE) {
                    start();
                    break;
                }
                if(currentState == State.STATE_PLAYING) {
                    pause();
                    break;
                }
                break;
            case android.R.id.button3:
                next();
                break;
            case R.id.action_setting:
                if(customRelativeLayout == null) {
                    customRelativeLayout = (CustomRelativeLayout) setting_layout.inflate();
                    initCustomSettingView();
                }
                customRelativeLayout.show();
                break;
            default:
                break;
        }
        press_animator = pressAnimator(view);
        press_animator.start();
    }

    private void initCustomSettingView() {
        customSettingView = (CustomSettingView) customRelativeLayout.getChildAt(0);
        customSettingView.setOnTextSizeChangeListener(new TextSizeChangeListener());
        customSettingView.setOnColorItemChangeListener(new ColorItemClickListener());
        customSettingView.setOnDismissBtnClickListener(new DismissBtnClickListener());
        customSettingView.setOnLineSpaceChangeListener(new LineSpaceChangeListener());
    }

    private class TextSizeChangeListener implements SeekBar.OnSeekBarChangeListener {

        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            if(fromUser) {
                lyricView.setTextSize(15.0f + 3 * progress / 100.0f);
            }
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {

        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
            PreferenceUtil.getInstance(MainActivity.this).putFloat(PreferenceUtil.KEY_TEXT_SIZE, 15.0f + 3 * seekBar.getProgress() / 100.0f);
        }
    }

    private class LineSpaceChangeListener implements SeekBar.OnSeekBarChangeListener {

        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            if(fromUser) {
                lyricView.setLineSpace(12.0f + 3 * progress / 100.0f);
            }
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {

        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
            PreferenceUtil.getInstance(MainActivity.this).putFloat(PreferenceUtil.KEY_LINE_SPACE, 12.0f + 3 * seekBar.getProgress() / 100.0f);
        }
    }

    private class DismissBtnClickListener implements View.OnClickListener {

        @Override
        public void onClick(View view) {
            if(customRelativeLayout != null) {
                customRelativeLayout.dismiss();
            }
        }
    }

    private class ColorItemClickListener implements CustomSettingView.OnColorItemChangeListener {

        @Override
        public void onColorChanged(int color) {
            lyricView.setHighLightTextColor(color);
            PreferenceUtil.getInstance(MainActivity.this).putInt(PreferenceUtil.KEY_HIGHLIGHT_COLOR, color);
            if(customRelativeLayout != null) {
                customRelativeLayout.dismiss();
            }
        }
    }

    public ValueAnimator pressAnimator(final View view) {
        final float size = view.getScaleX();
        ValueAnimator animator = ValueAnimator.ofFloat(size, size * 0.7f);
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {

            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                ViewHelper.setScaleX(view, (Float) animation.getAnimatedValue());
                ViewHelper.setScaleY(view, (Float) animation.getAnimatedValue());
            }
        });
        animator.addListener(new AnimatorListenerAdapter() {

            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                ViewHelper.setScaleX(view, size * 0.7f);
                ViewHelper.setScaleY(view, size * 0.7f);
                up_animator = upAnimator(view);
                up_animator.start();
            }

            @Override
            public void onAnimationCancel(Animator animation) {
                super.onAnimationCancel(animation);
                ViewHelper.setScaleX(view, size * 0.7f);
                ViewHelper.setScaleY(view, size * 0.7f);
            }
        });
        animator.setDuration(animatorDuration);
        return animator;
    }

    public ValueAnimator upAnimator(final View view) {
        final float size = view.getScaleX();
        ValueAnimator animator = ValueAnimator.ofFloat(size, size * 10 / 7.00f);
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {

            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                ViewHelper.setScaleX(view, (Float) animation.getAnimatedValue());
                ViewHelper.setScaleY(view, (Float) animation.getAnimatedValue());
            }
        });
        animator.addListener(new AnimatorListenerAdapter() {

            @Override
            public void onAnimationStart(Animator animation) {
                super.onAnimationStart(animation);
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                ViewHelper.setScaleX(view, size * 10 / 7.00f);
                ViewHelper.setScaleY(view, size * 10 / 7.00f);
            }

            @Override
            public void onAnimationCancel(Animator animation) {
                super.onAnimationCancel(animation);
                ViewHelper.setScaleX(view, size * 10 / 7.00f);
                ViewHelper.setScaleY(view, size * 10 / 7.00f);
            }
        });
        animator.setDuration(animatorDuration);
        return animator;
    }

    private enum State {
        STATE_STOP,STATE_SETUP,STATE_PREPARE,STATE_PLAYING,STATE_PAUSE;
    }
}
