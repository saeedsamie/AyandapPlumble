package com.morlunk.mumbleclient.app;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.SpannableString;
import android.text.TextWatcher;
import android.text.style.UnderlineSpan;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.morlunk.mumbleclient.R;
import com.morlunk.mumbleclient.ServerFetchAsync;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import java.util.ArrayList;
import java.util.List;

public class VerificationActivity extends AppCompatActivity {

    public TextView timer;
    public EditText vcode;
    public int isFinished = 0;
    boolean isUser;
    String user_phone_number;
    private String c = "";
    VerificationActivity verificationActivity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.verification);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            getWindow().getDecorView().setLayoutDirection(View.LAYOUT_DIRECTION_RTL);
        }
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);

        timer = (TextView) findViewById(R.id.timer);
//        backtologin = (TextView) findViewById(R.id.backtologin);
        vcode = (EditText) findViewById(R.id.vcode);
        Intent intent = getIntent();
        isUser = intent.getBooleanExtra("isUser", false);
        user_phone_number = intent.getStringExtra("Phone_number");
        verificationActivity = this;

        List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
        nameValuePairs.add(new BasicNameValuePair("func", "verification"));
        new ServerFetchAsync(nameValuePairs, this).execute();

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
                    AlertDialog.Builder alertBuilder;
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
                            alertBuilder = new AlertDialog.Builder(VerificationActivity.this);
                            alertBuilder.setMessage("کد وارد شده معتبر نیست!");
                            alertBuilder.setCancelable(false);
                            alertBuilder.setNeutralButton("باشه", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    vcode.setText("");
                                }
                            });
                            alertBuilder.show();
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
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}