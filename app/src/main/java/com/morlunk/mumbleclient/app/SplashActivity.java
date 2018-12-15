package com.morlunk.mumbleclient.app;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;

import com.morlunk.mumbleclient.R;

public class SplashActivity extends AppCompatActivity {

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_splash);

    new Handler().postDelayed(new Runnable() {
      @Override
      public void run() {

        Intent myIntent = new Intent(SplashActivity.this, WelcomeActivity.class);
//        ActivityOptions options =
//          ActivityOptions.makeCustomAnimation(SplashActivity.this, R.anim.fade_in, R.anim.fade_out);
//        SplashActivity.this.startActivity(myIntent, options.toBundle());
        startActivity(myIntent);
        finish();
      }
    }, 2200);

  }
}