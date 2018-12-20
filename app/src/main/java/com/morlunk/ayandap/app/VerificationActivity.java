package com.morlunk.ayandap.app;

import android.Manifest;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.provider.Settings;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.telephony.SmsManager;
import android.text.Editable;
import android.text.SpannableString;
import android.text.TextWatcher;
import android.text.style.UnderlineSpan;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.TextView;

import com.morlunk.ayandap.OnTaskCompletedListener;
import com.morlunk.ayandap.R;
import com.morlunk.ayandap.ServerFetchAsync;
import com.morlunk.ayandap.calligraphy.CalligraphyContextWrapper;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static com.morlunk.ayandap.app.LoginActivity.sharedPreferences;


public class VerificationActivity extends AppCompatActivity implements OnTaskCompletedListener {

    private static final int PERMISSION_REQUEST_CODE = 1;
    public static String sended;
    public TextView timer;
    public EditText vcode;
    public int isFinished = 0;
    public String android_id;
    public String uuid;
    public int flag = 0;
    public String body;
    public String numInsideSms;
    boolean isUser;
    String user_phone_number;
    VerificationActivity verificationActivity;
    Context context;
    private String c = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.verification);

        Intent intent = getIntent();
        if (intent == null)
            finish();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            getWindow().getDecorView().setLayoutDirection(View.LAYOUT_DIRECTION_RTL);
        }

        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);

        timer = findViewById(R.id.timer);
        vcode = findViewById(R.id.vcode);
        isUser = intent.getBooleanExtra("isUser", false);
        user_phone_number = intent.getStringExtra("Phone_number");
        verificationActivity = this;

        List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
        nameValuePairs.add(new BasicNameValuePair("func", "verification"));
        new ServerFetchAsync(nameValuePairs, this).execute();

        context = this;
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
//            defaultSmsApp = Telephony.Sms.getDefaultSmsPackage(context);
//        }


//        SmsReceiver.bindListener(new SmsListener() {
//            @Override
//            public void onMessageReceived(String messageText) {
//                Log.e("Text", messageText);
//                vcode.setText(messageText);
//            }
//        });


        android_id = Settings.Secure.getString(getApplicationContext().getContentResolver(),
                Settings.Secure.ANDROID_ID);
        uuid = UUID.randomUUID().toString();

        requestPermission();


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
                timer.setTextColor(Color.parseColor("#ffffff"));
                timer.setText(String.format("%s (%d)  ", getString(R.string.verificationPageNote), millisUntilFinished / 1000));
//                if (flag == 0) {
////                    searchSmsForCode();
//                }
            }

            public void onFinish() {
                SpannableString content = new SpannableString("کدی دریافت نکردید ؟");
                content.setSpan(new UnderlineSpan(), 0, content.length(), 0);
                timer.setText(content);
                timer.setTextColor(Color.parseColor("#ffffff"));
                isFinished = 1;
            }
        }.start();
        vcode.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public synchronized void onTextChanged(CharSequence s, int start, int before, int count) {

                if (s.length() >= 4) {
                    Intent intent;
                    String enteredCode = vcode.getText().toString().trim();
                    if (enteredCode.equals(c) && !isUser) {
                        intent = new Intent(VerificationActivity.this, SignupActivity.class);
                        intent.putExtra("phone_number", user_phone_number);
                        intent.putExtra("Launcher", "verification");
                        startActivity(intent);
                        verificationActivity.setResult(RESULT_OK);
                        finish();
                    } else if (enteredCode.equals(c) && isUser) {
                        intent = new Intent(VerificationActivity.this, PlumbleActivity.class);
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.putBoolean(getString(R.string.PREF_TAG_isLoggedIn), true);
                        editor.apply();
                        startActivity(intent);
                        verificationActivity.setResult(RESULT_OK);
                        finish();
                    } else {
                        Snackbar.make(findViewById(android.R.id.content), "کد وارد شده صحیح نیست", Snackbar.LENGTH_SHORT).show();
                        vcode.setError("کد وارد شده صحیح نیست!");
                    }
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }


    private void requestPermission() {
        ActivityCompat.requestPermissions(VerificationActivity.this, new String[]{
                Manifest.permission.SEND_SMS}, PERMISSION_REQUEST_CODE);
    }


    private void sendFakeSms() {
        SmsManager sms = SmsManager.getDefault();
        sended = "Ayandap Plumble - your code is : " + c;
        sms.sendTextMessage(LoginActivity.user_phone_number, null,
                String.valueOf(sended), null, null);
    }


    public void searchSmsForCode() {

        // Create Inbox box URI
        Uri inboxURI = Uri.parse("content://sms/inbox");

        // List required columns
        String[] reqCols = new String[]{"body"};

        // Get Content Resolver object, which will deal with Content
        // Provider
        ContentResolver contentResolver = getContentResolver();

        // Fetch Inbox SMS Message from Built-in Content Provider
        Cursor cursor = contentResolver.query(inboxURI, reqCols, null, null,
                null);

        //string to check
        String check = sended;

        while (cursor.moveToNext()) {
            //to get massage body
            body = cursor.getString(cursor.getColumnIndexOrThrow("body"));

            try {
                if (body.contains(check)) {

                    numInsideSms = body.substring(33);
                    vcode.setText(numInsideSms);
                    flag = 1;
                    //do STH

                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onTaskCompleted(JSONObject jsonObject) {
        try {
            c = jsonObject.getString("vcode");
//            c = code;
            //todo change String code to JSONObject
            TextView textView = findViewById(R.id.verification_code_holder);
            textView.setText(c);
//            sendFakeSms();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

    @Override
    protected void onResume() {
        if (getIntent() == null) finish();
        super.onResume();
    }
}