package com.morlunk.mumbleclient;

import android.app.Application;
import android.support.multidex.MultiDex;

import com.jakewharton.picasso.OkHttp3Downloader;
import com.squareup.picasso.Picasso;

import uk.co.chrisjenx.calligraphy.CalligraphyConfig;

public class FontChanger extends Application {

  @Override
  public void onCreate() {
    super.onCreate();

    Picasso.Builder builder = new Picasso.Builder(this);
    builder.downloader(new OkHttp3Downloader(this,Integer.MAX_VALUE));
    Picasso built = builder.build();
    built.setIndicatorsEnabled(true);
    built.setLoggingEnabled(true);
    Picasso.setSingletonInstance(built);

    MultiDex.install(this);
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