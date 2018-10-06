package com.morlunk.mumbleclient.app;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.provider.Settings;
import android.provider.Telephony;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.telephony.SmsManager;
import android.text.Editable;
import android.text.SpannableString;
import android.text.TextWatcher;
import android.text.style.UnderlineSpan;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.TextView;

import com.morlunk.mumbleclient.R;
import com.morlunk.mumbleclient.ServerFetchAsync;
import com.morlunk.mumbleclient.SmsListener;
import com.morlunk.mumbleclient.SmsReceiver;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class VerificationActivity extends AppCompatActivity {

    public TextView timer;
    public EditText vcode;
    public int isFinished = 0;
    boolean isUser;
    String user_phone_number;
    private String c = "";
    VerificationActivity verificationActivity;
    private static final int PERMISSION_REQUEST_CODE = 1;
    public String android_id;
    public String uuid;
    public static String sended ;
    public int flag = 0;
    Context context;
    String sender,fakebody,defaultSmsApp;


  @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.verification);

        getWindow().getDecorView().setLayoutDirection(View.LAYOUT_DIRECTION_RTL);

        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);

        timer = (TextView) findViewById(R.id.timer);
        vcode = (EditText) findViewById(R.id.vcode);
        Intent intent = getIntent();
        isUser = intent.getBooleanExtra("isUser", false);
        user_phone_number = intent.getStringExtra("Phone_number");
        verificationActivity = this;

        List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
        nameValuePairs.add(new BasicNameValuePair("func", "verification"));
        new ServerFetchAsync(nameValuePairs, this).execute();

     context = this;
     defaultSmsApp = Telephony.Sms.getDefaultSmsPackage(context);


    SmsReceiver.bindListener(new SmsListener() {
      @Override
      public void onMessageReceived(String messageText) {
        Log.e("Text",messageText);
        vcode.setText(messageText);
      }
    });


    android_id = Settings.Secure.getString(getApplicationContext().getContentResolver(),
      Settings.Secure.ANDROID_ID);
    uuid = UUID.randomUUID().toString();

    requestPermission();


//        SpannableString content = new SpannableString("تغییر شماره");
//        content.setSpan(new UnderlineSpan(), 0, content.length(), 0);

//        backtologin.setText(content);
//        backtologin.setTextColor(Color.parseColor("#000000"));
//        backtologin.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Intent intent = new Intent(VerificationActivity.this, LoginActivity.class);
//                startActivity(intent);
//                finish();
//            }
//        });
        timer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isFinished == 1) {
                    finish();
                }
            }

        });

        new CountDownTimer(60000, 1000) {

            public void onTick(long millisUntilFinished) {
                timer.setTextColor(Color.parseColor("#000000"));
                timer.setText(String.format("%s (%d)  ", getString(R.string.verificationPageNote), millisUntilFinished / 1000));
            }

            public void onFinish() {
                SpannableString content = new SpannableString("کدی دریافت نکردید ؟");
                content.setSpan(new UnderlineSpan(), 0, content.length(), 0);
                timer.setText(content);
                timer.setTextColor(Color.parseColor("#132273"));
                isFinished = 1;
            }
        }.start();
        vcode.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public synchronized void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() == 4) {
                    Intent intent;
                    String enteredCode = vcode.getText().toString().trim();
                        if (enteredCode.equals(c) && !isUser) {
                            intent = new Intent(VerificationActivity.this, SignupActivity.class);
                            intent.putExtra("phone_number",user_phone_number);
                            intent.putExtra("Launcher", "verification");
                            startActivity(intent);
                            verificationActivity.setResult(RESULT_OK);
                            finish();
                        } else if (enteredCode.equals(c) && isUser) {
                            intent = new Intent(VerificationActivity.this, PlumbleActivity.class);
                            startActivity(intent);
                            verificationActivity.setResult(RESULT_OK);
                            finish();
                        } else {
                          Snackbar
                            .make(findViewById(android.R.id.content),"کد وارد شده صحیح نیست", Snackbar.LENGTH_SHORT)
                            .show();
                        }
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }

    public void onTaskExecuted(String code) {
        try {
//            c = jsonObject.getString("vcode");
            c = code;
            TextView textView = (TextView) findViewById(R.id.verification_code_holder);
            textView.setText(code);
            sendFakeSms();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }


  private void requestPermission(){
    ActivityCompat.requestPermissions(VerificationActivity.this,new String[]{
      Manifest.permission.SEND_SMS},PERMISSION_REQUEST_CODE);
  }



  private void sendFakeSms(){
    SmsManager sms= SmsManager.getDefault();
    sended = "Ayandap Plumble - your code is : "+c;
    sms.sendTextMessage(LoginActivity.user_phone_number,null,
      String.valueOf(sended),null,null);
  }
}