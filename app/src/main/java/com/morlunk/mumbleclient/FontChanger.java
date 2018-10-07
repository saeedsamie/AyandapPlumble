package com.morlunk.mumbleclient;

import android.app.Application;

import uk.co.chrisjenx.calligraphy.CalligraphyConfig;

public class FontChanger extends Application {

  @Override
  public void onCreate() {
    super.onCreate();
    CalligraphyConfig.initDefault(new CalligraphyConfig.Builder()
      .setDefaultFontPath("iransans.ttf")
      .setFontAttrId(R.attr.fontPath)
      .build()
    );
  }

  public void fontChanger(String font)
  {
    CalligraphyConfig.initDefault(new CalligraphyConfig.Builder()
      .setDefaultFontPath(font+ ".ttf")
      .setFontAttrId(R.attr.fontPath)
      .build()
    );
  }

}