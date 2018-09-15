package com.morlunk.mumbleclient.app;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.v7.app.AppCompatActivity;
import android.text.SpannableString;
import android.text.style.UnderlineSpan;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.morlunk.mumbleclient.R;

import java.util.List;

public class Verification extends AppCompatActivity {

  public TextView timer;
  public TextView backtologin;
  public Button enter;
  public String body;
  public String numInsideSms;
  public EditText vcode;
  public int flag = 0;
  public int isFinished = 0;
  Context context;
  String sender,fakebody,defaultSmsApp;

  private static final int PERMISSION_REQUEST_CODE = 1;
  public String android_id;
  public String uuid;
  public static String sended ;
  SharedPreferences sp;
  public static final String Name = "tHiS_PHoNeNuMbEr";

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.verification);
    getWindow().getDecorView().setLayoutDirection(View.LAYOUT_DIRECTION_RTL);
    getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
    timer = (TextView) findViewById(R.id.timer);
    backtologin = (TextView) findViewById(R.id.backtologin);
    enter = (Button) findViewById(R.id.enter);
    vcode = (EditText) findViewById(R.id.vcode);
    sp = getSharedPreferences("MyPref", Context.MODE_PRIVATE);

    context = this;

    SpannableString content = new SpannableString("تغییر شماره");
    content.setSpan(new UnderlineSpan(), 0, content.length(), 0);
    backtologin.setText(content);
    backtologin.setTextColor(Color.parseColor("#000000"));

    backtologin.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        Intent intent = new Intent(Verification.this, Login.class);
        startActivity(intent);
        finish();
      }
    });
    timer.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        if (isFinished == 1) {
          new CountDownTimer(60000, 1000) {

            public void onTick(long millisUntilFinished) {

              timer.setTextColor(Color.parseColor("#000000"));
              timer.setText("برای ادامه لطفا کد جدیدی که برای شما ارسال کرده ایم را وارد نمایید" + " " + "(" + millisUntilFinished / 1000 + ")");
              isFinished = 0;
              //here you can have your logic to set text to edittext
            }

            public void onFinish() {
              SpannableString content = new SpannableString("کدی دریافت نکردید ؟");
              content.setSpan(new UnderlineSpan(), 0, content.length(), 0);
              timer.setText(content);
              timer.setTextColor(Color.parseColor("#132273"));
              isFinished = 1;
              Log.i("testti", "" + isFinished);
            }

          }.start();

          enter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

              Intent intent;
              if (!Login.isUser)
              {
                intent = new Intent(Verification.this, Signup.class);
                startActivity(intent);
              }
              else if(Login.isUser) {
                Log.i("XShBXSHbhXS", "2");
                intent = new Intent(Verification.this, PlumbleActivity.class);
                SharedPreferences.Editor sEdit = sp.edit();
                sEdit.putString(Name, Login.user_phone);
                sEdit.apply();
                startActivity(intent);
              }
              finish();

            }
          });

        }
      }

    });

    new CountDownTimer(60000, 1000) {

      public void onTick(long millisUntilFinished) {
        timer.setTextColor(Color.parseColor("#000000"));
        timer.setText("   برای ادامه لطفا کدی که برای شما ارسال کرده ایم را وارد نمایید" + " " + "(" + millisUntilFinished / 1000 + ")  ");
      }

      public void onFinish() {
        SpannableString content = new SpannableString("کدی دریافت نکردید ؟");
        content.setSpan(new UnderlineSpan(), 0, content.length(), 0);
        timer.setText(content);
        timer.setTextColor(Color.parseColor("#132273"));
        isFinished = 1;
      }
    }.start();

    enter.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        Intent intent;
        if (!Login.isUser)
        {
          intent = new Intent(Verification.this, Signup.class);
          startActivity(intent);
        }
        else if(Login.isUser) {
          intent = new Intent(Verification.this, PlumbleActivity.class);
          SharedPreferences.Editor sEdit = sp.edit();
          sEdit.putString(Name, Login.user_phone);
          sEdit.apply();
          startActivity(intent);
        }
        finish();

      }
    });
  }
}