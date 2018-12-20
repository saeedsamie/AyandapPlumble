package com.morlunk.ayandap;

import android.app.Application;
import android.content.Context;
import android.support.multidex.MultiDex;

import com.morlunk.ayandap.calligraphy.CalligraphyConfig;


public class FontChanger extends Application {

    @Override
    public void onCreate() {
        super.onCreate();


        MultiDex.install(this);
        CalligraphyConfig.initDefault(new CalligraphyConfig.Builder()
                .setDefaultFontPath("iransans.ttf")
                .setFontAttrId(R.attr.fontPath)
                .build()
        );
    }

    public void fontChanger(String font) {
        CalligraphyConfig.initDefault(new CalligraphyConfig.Builder()
                .setDefaultFontPath(font + ".ttf")
                .setFontAttrId(R.attr.fontPath)
                .build()
        );
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
    }

}