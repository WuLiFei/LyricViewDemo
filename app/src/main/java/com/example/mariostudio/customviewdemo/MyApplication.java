package com.example.mariostudio.customviewdemo;

import android.app.Application;

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
