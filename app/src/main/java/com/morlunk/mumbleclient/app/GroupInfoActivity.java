package com.morlunk.mumbleclient.app;


import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.jakewharton.picasso.OkHttp3Downloader;
import com.morlunk.mumbleclient.OnTaskCompletedListener;
import com.morlunk.mumbleclient.R;
import com.morlunk.mumbleclient.ServerFetchAsync;
import com.squareup.picasso.Picasso;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import jp.wasabeef.picasso.transformations.CropCircleTransformation;
import okhttp3.Cache;
import okhttp3.OkHttpClient;

public class GroupInfoActivity extends AppCompatActivity {

  private ListView listView;
  private TextView bio;
  private ImageView image;
  public static Picasso picassoWithCache;
  List<NameValuePair> nameValuePairs;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_group_info);
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
      getWindow().getDecorView().setLayoutDirection(View.LAYOUT_DIRECTION_RTL);
    }

    listView = findViewById(R.id.group_info_listview);
    bio = findViewById(R.id.group_info_bio);
    image = findViewById(R.id.group_info_image);

    Toolbar toolbar = (Toolbar)findViewById(R.id.group_info_toolbar);
    setSupportActionBar(toolbar);
    if(getSupportActionBar() != null)
      getSupportActionBar().setDisplayHomeAsUpEnabled(true);

    String chatTitle = getIntent().getStringExtra("ChatTitle");
    this.setTitle(chatTitle);
    bio.setText(getIntent().getStringExtra("bio"));
    File httpCacheDirectory = new File(this.getCacheDir(), "picasso-cache");
    Cache cache = new Cache(httpCacheDirectory, 15 * 1024 * 1024);
    OkHttpClient.Builder okHttpClientBuilder = new OkHttpClient.Builder().cache(cache);
    picassoWithCache = new Picasso.Builder(this).downloader(new OkHttp3Downloader(okHttpClientBuilder.build())).build();
    picassoWithCache.load(LoginActivity.URL+"profile_image/" + "def" + ".png").transform(new CropCircleTransformation()).into(image);

    nameValuePairs = new ArrayList<NameValuePair>();
    nameValuePairs.add(new BasicNameValuePair("func", "getUsers"));
    nameValuePairs.add(new BasicNameValuePair("chatId", getIntent().getStringExtra("chatId")));
    new ServerFetchAsync(nameValuePairs, new OnTaskCompletedListener() {
      @Override
      public void onTaskCompleted(JSONObject jsonObject) {
        final ArrayList<HashMap<String, String>> listValues = new ArrayList<>();
        try {
          JSONArray jsonArray;
          jsonArray = jsonObject.getJSONArray("getUsers");

          for (int i = 0; i < jsonArray.length(); i++) {
            HashMap hashMap = new HashMap();
            JSONObject c = jsonArray.getJSONObject(i);

            hashMap.put("name", c.getString("fullname"));
            hashMap.put("role", c.getString("role"));
            hashMap.put("image", LoginActivity.URL + "profile_image/" + c.getString("image"));
            hashMap.put("id", c.getString("id"));

            listValues.add(hashMap);
          }

          ChatUsersListAdapter adapter = new ChatUsersListAdapter(GroupInfoActivity.this, listValues);
          listView.setAdapter(adapter);

        } catch (Exception e) {
          e.printStackTrace();
        }


    }}).execute();

  }
}