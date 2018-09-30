package com.morlunk.mumbleclient.app;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.morlunk.mumbleclient.R;
import com.morlunk.mumbleclient.ServerFetchAsync;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;


public class LoginActivity extends AppCompatActivity {

  public static String responseLogin;
  public static JSONObject jsonResponse;
  private TextView textView;
  public Button loginContinue ;
  public EditText phone ;
  public String responseMain;
  public static String user_phone;
  public static String user_fullName;
  SharedPreferences sp;
  public static boolean isUser;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.login);

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
      getWindow().getDecorView().setLayoutDirection(View.LAYOUT_DIRECTION_RTL);
    }
    loginContinue = (Button) findViewById(R.id.login_continue);
    phone = (EditText) findViewById(R.id.login_phone);
    sp = getSharedPreferences("MyPref", Context.MODE_PRIVATE);

    if (sp.contains(SignupActivity.Username_Tag))
    {
      SignupActivity.username = sp.getString(SignupActivity.Username_Tag,null);
    }

    if (sp.contains(VerificationActivity.Name_Tag)) {
      user_phone = sp.getString(VerificationActivity.Name_Tag, null);
      Intent intent = new Intent(LoginActivity.this,PlumbleActivity.class);
      startActivity(intent);
      finish();

    }


    loginContinue.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
//        backgroundTask = new BackgroundTask(textView);
//        backgroundTask.execute();

        List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
        nameValuePairs.add(new BasicNameValuePair("func", "0"));
        nameValuePairs.add(new BasicNameValuePair("phone", phone.getText().toString()));
        ServerFetchAsync serverFetchAsync = new ServerFetchAsync(nameValuePairs);
        JSONObject jsonObject;

        serverFetchAsync.execute();

        try {
          jsonObject = serverFetchAsync.getJsonResponse();
          isUser = jsonObject.getString("result").equals("1");
          user_fullName = jsonObject.getString("fullName");
        } catch (Exception e) {
          e.printStackTrace();
        }

        user_phone = phone.getText().toString();
        Intent intent = new Intent(LoginActivity.this, VerificationActivity.class);
        startActivity(intent);
        finish();
      }
    });
  }
}