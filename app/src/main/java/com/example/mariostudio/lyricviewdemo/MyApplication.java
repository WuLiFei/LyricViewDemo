package com.example.mariostudio.lyricviewdemo;

import android.app.Application;

import com.example.mariostudio.lyricviewdemo.Content;

import java.io.File;

/**
 * Created by MarioStudio on 2016/7/18.
 */

public class MyApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        File file = new File(Content.lyricPath);
        if(!file.exists()) {
            file.mkdirs();
        }
    }
}
