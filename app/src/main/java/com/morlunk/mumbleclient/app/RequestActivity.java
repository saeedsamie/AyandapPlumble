package com.morlunk.mumbleclient.app;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.morlunk.mumbleclient.OnTaskCompletedListener;
import com.morlunk.mumbleclient.R;
import com.morlunk.mumbleclient.ServerFetchAsync;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class RequestActivity extends AppCompatActivity {

  SharedPreferences sharedPreferences;
  private ListView requests_list;
  private ArrayList<HashMap<String, String>> listValues;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.popup_requests);
    sharedPreferences = getSharedPreferences("MyPref", Context.MODE_PRIVATE);
    String id = sharedPreferences.getString(getString(R.string.PREF_TAG_userid),"-2");


    List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
    nameValuePairs.add(new BasicNameValuePair("func", "requests"));
    nameValuePairs.add(new BasicNameValuePair("userId", id));
    new ServerFetchAsync(nameValuePairs, new OnTaskCompletedListener() {
      @Override
      public void onTaskCompleted(JSONObject jsonObject) {
        Log.i("fjfdsnfsdfsdkjnsakjfnak",jsonObject.toString());
        listValues = new ArrayList<>();
        try {
          JSONArray jsonArray;
          jsonArray = jsonObject.getJSONArray("requests");

          for (int i = 0; i < jsonArray.length(); i++) {
            HashMap hashMap = new HashMap();
            JSONObject c = jsonArray.getJSONObject(i);
            hashMap.put("fullname", c.getString("fullname"));
            hashMap.put("username", c.getString("username"));
            hashMap.put("chatId", c.getString("chatId"));
            listValues.add(hashMap);
          }

        } catch (Exception e) {
          e.printStackTrace();
        }




        RequestListAdapter adapter = new RequestListAdapter(RequestActivity.this, listValues);
        requests_list.setAdapter(adapter);
        adapter.notifyDataSetChanged();


      }
    }).execute();

    requests_list =(ListView) findViewById(R.id.requests_list);
    requests_list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
      @Override
      public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

      }
    });
  }
}
