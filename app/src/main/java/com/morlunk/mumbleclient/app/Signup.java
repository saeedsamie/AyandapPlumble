package com.morlunk.mumbleclient.app;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.morlunk.mumbleclient.R;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

public class Signup extends AppCompatActivity {

  EditText fullname;
  EditText username;
  Button signup;
  public static String responseSignup;
  public static JSONObject jsonresponse;
  private TextView textView;
  private BackgroundTask backgroundTask;
  private ProgressDialog pDialog;
  SharedPreferences sp;
  public static final String Name = "tHiS_PHoNeNuMbEr";

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.signup);
    getWindow().getDecorView().setLayoutDirection(View.LAYOUT_DIRECTION_RTL);
    fullname = (EditText) findViewById(R.id.signup_fullname);
    username = (EditText) findViewById(R.id.signup_username);
    signup = (Button) findViewById(R.id.signup_signup);
    sp = getSharedPreferences("MyPref", Context.MODE_PRIVATE);

    signup.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        backgroundTask = new BackgroundTask(textView);
        backgroundTask.execute();
      }
    });
  }

  private class BackgroundTask extends AsyncTask<Void, Void, String> {

    private final WeakReference<TextView> messageViewReference;
    private BackgroundTask(TextView textView) {
      this.messageViewReference = new WeakReference<>(textView);
    }

    @Override
    protected void onPreExecute() {
      super.onPreExecute();
      pDialog = new ProgressDialog(Signup.this);
      pDialog.setMessage("لطفا صبر کنید...");
      pDialog.setCancelable(false);
      pDialog.show();

    }

    @Override
    protected String doInBackground(Void... voids) {

      HttpClient httpclient = new DefaultHttpClient();
      HttpPost httppost = new HttpPost("http://192.168.2.26/Plumble/fetchData.php");
      try {
        List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
        nameValuePairs.add(new BasicNameValuePair("func", "2"));
        nameValuePairs.add(new BasicNameValuePair("phone", Login.user_phone));
        nameValuePairs.add(new BasicNameValuePair("fullname", fullname.getText().toString()));
        nameValuePairs.add(new BasicNameValuePair("username", username.getText().toString()));
        Log.e("mainToPost", "mainToPost" + nameValuePairs.toString());
        UrlEncodedFormEntity form;
        form = new UrlEncodedFormEntity(nameValuePairs,"UTF-8");
        httppost.setEntity(form);
        HttpResponse response = httpclient.execute(httppost);
        InputStream inputStream = response.getEntity().getContent();
        Signup.InputStreamToStringExample str = new Signup.InputStreamToStringExample();
        responseSignup = str.getStringFromInputStream(inputStream);
        Log.e("response", "response -----" + responseSignup);
        jsonresponse = new JSONObject(responseSignup);
      } catch (Exception e) {
        e.printStackTrace();
      }
      return null;
    }

    @Override
    protected void onPostExecute(String s) {
      super.onPostExecute(s);
      TextView textView = messageViewReference.get();
      if(textView != null) {
        textView.setText(s);
      }
      pDialog.dismiss();
      SharedPreferences.Editor sEdit = sp.edit();
      sEdit.putString(Name, Login.user_phone);
      sEdit.apply();
      Intent intent = new Intent(Signup.this,PlumbleActivity.class);
      startActivity(intent);
    }
  }

  public static class InputStreamToStringExample {

    public static void main(String[] args) throws IOException {

      // intilize an InputStream
      InputStream is =
        new ByteArrayInputStream("file content..blah blah".getBytes());

      String result = getStringFromInputStream(is);

      System.out.println(result);
      System.out.println("Done");

    }

    public static String getStringFromInputStream(InputStream is) {

      BufferedReader br = null;
      StringBuilder sb = new StringBuilder();
      String line;
      try {
        br = new BufferedReader(new InputStreamReader(is));
        while ((line = br.readLine()) != null) {
          sb.append(line);
        }
      } catch (IOException e) {
        e.printStackTrace();
      } finally {
        if (br != null) {
          try {
            br.close();
          } catch (IOException e) {
            e.printStackTrace();
          }
        }
      }
      return sb.toString();
    }
  }

}
