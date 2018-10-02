package com.morlunk.mumbleclient.app;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.morlunk.mumbleclient.R;
import com.morlunk.mumbleclient.ServerFetchAsync;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class SignupActivity extends AppCompatActivity {

    Button signup;
    SharedPreferences sp;
    AlertDialog.Builder alertBuilder;
    private EditText ed_fullname;
    private EditText ed_username;
    private ProgressDialog pDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.signup);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            getWindow().getDecorView().setLayoutDirection(View.LAYOUT_DIRECTION_RTL);
        }
        ed_fullname = (EditText) findViewById(R.id.signup_fullname);
        ed_username = (EditText) findViewById(R.id.signup_username);
        signup = (Button) findViewById(R.id.signup_signup);
        sp = getSharedPreferences("MyPref", Context.MODE_PRIVATE);
        pDialog = new ProgressDialog(SignupActivity.this);
        pDialog.setMessage("لطفا صبر کنید...");
        pDialog.setCancelable(false);
        alertBuilder = new AlertDialog.Builder(SignupActivity.this);
        final SignupActivity signupActivity = this;


        signup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                AlertDialog.Builder alertBuilder = new AlertDialog.Builder(SignupActivity.this);
                if (ed_fullname.getText().toString().matches("")) {

                    alertBuilder.setMessage("نام خود را وارد کنید!");
                    alertBuilder.setCancelable(false);
                    alertBuilder.setNeutralButton("باشه", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                        }
                    });
                    alertBuilder.show();
                } else if (ed_username.getText().toString().matches("")) {
                    alertBuilder.setMessage("نام کاربری خود را وارد کنید!");
                    alertBuilder.setCancelable(false);
                    alertBuilder.setNeutralButton("باشه", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                        }
                    });
                    alertBuilder.show();
                } else {
                    List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
                    nameValuePairs.add(new BasicNameValuePair("func", "register"));
                    nameValuePairs.add(new BasicNameValuePair("phone", getIntent().getStringExtra("phone_number")));
                    nameValuePairs.add(new BasicNameValuePair("fullname", ed_fullname.getText().toString()));
                    nameValuePairs.add(new BasicNameValuePair("username", ed_username.getText().toString()));
                    nameValuePairs.add(new BasicNameValuePair("image", ""));
                    Log.e("mainToPost", "mainToPost" + nameValuePairs.toString());
                    pDialog.show();
                    new ServerFetchAsync(nameValuePairs, signupActivity).execute();
                }
            }
        });
    }

    public void onTaskExecuted(JSONObject jsonObject) {

        SharedPreferences sharedPreferences = this.getSharedPreferences("MyPref", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(getString(R.string.PREF_TAG_isLoggedIn), true);
        editor.putString(getString(R.string.PREF_TAG_phonenumber), getIntent().getStringExtra("phone_number"));
        editor.putString(getString(R.string.PREF_TAG_fullname), ed_fullname.getText().toString());
        editor.putString(getString(R.string.PREF_TAG_username), ed_username.getText().toString());
        editor.putString(getString(R.string.PREF_TAG_image), ed_username.getText().toString());
        Boolean isSavedInPref = editor.commit();
        pDialog.dismiss();
        try {
            if (isSavedInPref && !jsonObject.getJSONArray("userId").getJSONObject(0).getString("userId").isEmpty()) {
                alertBuilder.setMessage("ثبت شد!");
                alertBuilder.setCancelable(false);
                alertBuilder.setNeutralButton("باشه", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (!getIntent().getStringExtra("Launcher").equals("main")) {
                            Intent intent = new Intent(SignupActivity.this, PlumbleActivity.class);
                            startActivity(intent);
                            finish();
                        }
                    }
                });
                alertBuilder.show();
            } else {
                alertBuilder.setMessage("دوباره سعی کنید!");
                alertBuilder.setCancelable(false);
                alertBuilder.setNeutralButton("باشه", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                });
                alertBuilder.show();
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
