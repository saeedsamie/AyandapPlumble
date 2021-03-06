package com.morlunk.ayandap.app;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.morlunk.ayandap.OnTaskCompletedListener;
import com.morlunk.ayandap.R;
import com.morlunk.ayandap.ServerFetchAsync;
import com.morlunk.ayandap.calligraphy.CalligraphyContextWrapper;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class FinishUpChannelActivity extends AppCompatActivity{

  Button finish_up;
  EditText groupName;
  EditText groupBio;
  String toUsers;
  List<NameValuePair> nameValuePairs;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_finish_up_channel);

    toUsers = getIntent().getStringExtra("toUsers");
    finish_up = findViewById(R.id.group_finish_up_button);
    groupName = findViewById(R.id.finishup_groupname);
    groupBio = findViewById(R.id.finishup_groupbio);

    finish_up.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        SharedPreferences sharedPreferences = FinishUpChannelActivity.this.getSharedPreferences("MyPref", Context.MODE_PRIVATE);
        nameValuePairs = new ArrayList<NameValuePair>();
        nameValuePairs.add(new BasicNameValuePair("func", "createChannel"));
        nameValuePairs.add(new BasicNameValuePair("channelTitle", groupName.getText().toString()));
        nameValuePairs.add(new BasicNameValuePair("channelBio", groupBio.getText().toString()));
        nameValuePairs.add(new BasicNameValuePair("fromUser", sharedPreferences.getString(FinishUpChannelActivity.this.getString(R.string.PREF_TAG_userid), "0")));
        nameValuePairs.add(new BasicNameValuePair("toUsers", toUsers));
        new ServerFetchAsync(nameValuePairs, new OnTaskCompletedListener() {
          @Override
          public void onTaskCompleted(JSONObject jsonObject) {
            final ArrayList<HashMap<String, String>> listValues = new ArrayList<>();
            try {
              switch (jsonObject.getString("channel")) {
                case "CREATED":
                  Toast.makeText(FinishUpChannelActivity.this, "کانال مورد نظر ساخته شد", Toast.LENGTH_LONG).show();
                  finish();
                  break;
              }
            } catch (Exception e) {
              e.printStackTrace();
            }
          }
        }).execute();
      }
    });
  }
  @Override
  protected void attachBaseContext(Context newBase) {
    super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
  }
}
