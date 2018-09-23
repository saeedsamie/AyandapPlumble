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
  private BackgroundTask backgroundTask;
  private BackgroundTask1 backgroundTask1;
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
          jsonObject = serverFetchAsync.get();
          isUser = jsonObject.getString("result").equals("1");
          user_fullName = jsonObject.getString("fullName");
        } catch (JSONException e) {
          e.printStackTrace();
        } catch (InterruptedException e) {
          e.printStackTrace();
        } catch (ExecutionException e) {
          e.printStackTrace();
        }catch (Exception e) {
          e.printStackTrace();
        }

        user_phone = phone.getText().toString();
        Intent intent = new Intent(LoginActivity.this, VerificationActivity.class);
        startActivity(intent);
        finish();
      }
    });
  }

  private class BackgroundTask extends AsyncTask<Void, Void, String> {

    private final WeakReference<TextView> messageViewReference;
    private BackgroundTask(TextView textView) {
      this.messageViewReference = new WeakReference<>(textView);
    }


    @Override
    protected String doInBackground(Void... voids) {

      HttpClient httpClient = new DefaultHttpClient();
      HttpPost httpPost = new HttpPost("http://192.168.2.26/Plumble/fetchData.php");
      try {
        List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
        nameValuePairs.add(new BasicNameValuePair("func", "1"));
        nameValuePairs.add(new BasicNameValuePair("phone", phone.getText().toString()));

        Log.e("mainToPost", "mainToPost" + nameValuePairs.toString());

        UrlEncodedFormEntity form;
        form = new UrlEncodedFormEntity(nameValuePairs,"UTF-8");

        // Use UrlEncodedFormEntity to send in proper format which we need
        httpPost.setEntity(form);
        HttpResponse response = httpClient.execute(httpPost);
        InputStream inputStream = response.getEntity().getContent();
        LoginActivity.InputStreamToStringExample str = new LoginActivity.InputStreamToStringExample();
        responseLogin = str.getStringFromInputStream(inputStream);
        Log.e("response", "response -----" + responseLogin);
        jsonResponse = new JSONObject(responseLogin);
        responseMain = jsonResponse.toString();
        Log.i("SWKJNKJNWQSJKNKJN", responseMain);

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
      backgroundTask1 = new BackgroundTask1(textView);
      backgroundTask1.execute();
    }
  }

  private class BackgroundTask1 extends AsyncTask<Void, Void, String> {

    private final WeakReference<TextView> messageViewReference;
    private BackgroundTask1(TextView textView) {
      this.messageViewReference = new WeakReference<>(textView);
    }

    @Override
    protected String doInBackground(Void... voids) {

      JSONArray loginResponse = new JSONArray();
      try {
        if(jsonResponse!=null)
        loginResponse = jsonResponse.getJSONArray("reslogin");
        String result = "0";
        for (int i = 0; i < loginResponse.length(); i++) {
          ArrayList<Object> mapping = new ArrayList<>();
          JSONObject c = loginResponse.getJSONObject(i);
          result = c.getString("result");
          user_fullName = c.getString("fullName");
        }
        if (result.equals("1"))
        {
          isUser = true;
        }
        else
        {
          isUser = false;
        }
      } catch (JSONException e) {
        e.printStackTrace();
      }


      return null;
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

    // convert InputStream to String
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