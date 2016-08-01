package com.example.mariostudio.lyricviewdemo.activity;

import android.annotation.TargetApi;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import com.example.mariostudio.lyricviewdemo.Constant;
import com.example.mariostudio.lyricviewdemo.R;

import org.w3c.dom.Text;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by MarioStudio on 2016/7/31.
 */

public class OtherActivity extends AppCompatActivity {

    private TextView text;
    private View statueBar;

    private LyricInfo lyricInfo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_other);

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

        text = (TextView) findViewById(R.id.text);
    }

    private void initAllDatum() {
        TextView textView = new TextView(this);

        File file = new File(Constant.lyricPath + "一个人的北京 - 好妹妹乐队.lrc");
        if (file != null && file.exists()) {
            try {
                setupLyricResource(new FileInputStream(file), "GBK");
                StringBuffer stringBuffer = new StringBuffer();
                if(lyricInfo != null && lyricInfo.song_lines != null) {
                    int size = lyricInfo.song_lines.size();
                    for (int i = 0; i < size; i ++) {
                        stringBuffer.append(lyricInfo.song_lines.get(i).content + "\n");
                    }
                    text.setText(stringBuffer.toString());
                }
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }
    }

    private void setupLyricResource(InputStream inputStream, String charsetName) {
        if(inputStream != null) {
            try {
                lyricInfo = new LyricInfo();
                lyricInfo.song_lines = new ArrayList<>();
                InputStreamReader inputStreamReader = new InputStreamReader(inputStream, charsetName);
                BufferedReader reader = new BufferedReader(inputStreamReader);
                String line = null;
                while((line = reader.readLine()) != null) {
                    analyzeLyric(lyricInfo, line);
                }
                reader.close();
                inputStream.close();
                inputStreamReader.close();
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

    private long measureStartTimeMillis(String str) {
        long minute = Long.parseLong(str.substring(1, 3));
        long second = Long.parseLong(str.substring(4, 6));
        long millisecond = Long.parseLong(str.substring(7, 9));
        return millisecond + second * 1000 + minute * 60 * 1000;
    }

    class LyricInfo {
        List<LineInfo> song_lines;

        String song_artist;  // 歌手
        String song_title;  // 标题
        String song_album;  // 专辑

        long song_offset;  // 偏移量

    }

    class LineInfo {
        String content;  // 歌词内容
        long start;  // 开始时间
    }
}
