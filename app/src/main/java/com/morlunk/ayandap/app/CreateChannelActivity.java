package com.morlunk.ayandap.app;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;

import com.miguelcatalan.materialsearchview.MaterialSearchView;
import com.morlunk.ayandap.OnTaskCompletedListener;
import com.morlunk.ayandap.R;
import com.morlunk.ayandap.ServerFetchAsync;
import com.morlunk.ayandap.calligraphy.CalligraphyContextWrapper;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;




public class  CreateChannelActivity extends AppCompatActivity {
  MaterialSearchView searchView;
  ListView lstView;
  List<NameValuePair> nameValuePairs;
  Boolean chatCreated = false;
  private ListView listView;
  private static Button continue_button;
  String old;
  public static int position = -2;
  public static ArrayList<HashMap<String, String>> choosen = new ArrayList<>();
  RecyclerView mRecyclerView;
  RecyclerView.LayoutManager mLayoutManager;
  static RecyclerView.Adapter mAdapter;


  private static final String TAG = "MainActivity";

  //vars
  private ArrayList<String> mNames = new ArrayList<>();
  private ArrayList<String> mImageUrls = new ArrayList<>();



  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_create_channel);
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
      getWindow().getDecorView().setLayoutDirection(View.LAYOUT_DIRECTION_RTL);
    }

    choosen.clear();

    mRecyclerView = findViewById(R.id.recycler_view);
    mRecyclerView.setHasFixedSize(true);
    // The number of Columns
    mLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
    mRecyclerView.setLayoutManager(mLayoutManager);
    mAdapter = new RecyclerViewAdapter(CreateChannelActivity.this, choosen);
    mRecyclerView.setAdapter(mAdapter);

    mRecyclerView.setOnLongClickListener(new View.OnLongClickListener() {
      @Override
      public boolean onLongClick(View view) {
        finish();
        return false;
      }
    });

    Toolbar toolbar = findViewById(R.id.toolbar);
    setSupportActionBar(toolbar);
    getSupportActionBar().setTitle("جستجوی نام کاربری");
    toolbar.setTitleTextColor(Color.parseColor("#FFFFFF"));

    listView = findViewById(R.id.create_group_search_listview);

    searchView = findViewById(R.id.search_view);
    continue_button = findViewById(R.id.create_group_continue);
    continue_button.setVisibility(View.GONE);
    searchView.setOnSearchViewListener(new MaterialSearchView.SearchViewListener() {
      @Override
      public void onSearchViewShown() {

      }

      @Override
      public void onSearchViewClosed() {
        //If closed Search View , lstView will return default
      }
    });
    continue_button.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        List<String> userIds = getPropertyList(choosen,"id");
        String toUsers = "";
        for (int i = 0 ; i < userIds.size()-1;i++)
        {
          toUsers += userIds.get(i) + ",";
        }
        toUsers += userIds.get(userIds.size()-1);
        Intent intent = new Intent(CreateChannelActivity.this,FinishUpChannelActivity.class);
        intent.putExtra("toUsers",toUsers);
        startActivity(intent);
        finish();
      }
    });

    searchView.setOnQueryTextListener(new MaterialSearchView.OnQueryTextListener() {
      @Override
      public boolean onQueryTextSubmit(String query) {
        return false;
      }

      @Override
      public boolean onQueryTextChange(final String newText) {
        if (newText != null && !newText.isEmpty()) {
          old = newText;
          new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
              if(old.equals(newText)) {
                final String userId = getSharedPreferences("MyPref", Context.MODE_PRIVATE)
                  .getString(getString(R.string.PREF_TAG_userid), "-2");
                nameValuePairs = new ArrayList<NameValuePair>();
                nameValuePairs.add(new BasicNameValuePair("func", "search"));
                nameValuePairs.add(new BasicNameValuePair("username", newText));
                new ServerFetchAsync(nameValuePairs, new OnTaskCompletedListener() {
                  @Override
                  public void onTaskCompleted(JSONObject jsonObject) {
                    final ArrayList<HashMap<String, String>> listValues = new ArrayList<>();
                    try {
                      JSONArray jsonArray;
                      jsonArray = jsonObject.getJSONArray("search");

                      for (int i = 0; i < jsonArray.length(); i++) {
                        HashMap hashMap = new HashMap();
                        JSONObject c = jsonArray.getJSONObject(i);

                        hashMap.put("fullname", c.getString("fullname"));
                        hashMap.put("username", c.getString("username"));
                        hashMap.put("image", LoginActivity.URL + "profile_image/" + c.getString("image"));
                        hashMap.put("id", c.getString("id"));
                        List<String> userIds = getPropertyList(choosen,"id");
                        if (userIds.contains(c.getString("id")) ) {
                          hashMap.put("selected", "1");
                        }
                        else
                        {
                          hashMap.put("selected", "0");
                        }


                        if (!c.getString("id").equals(userId))
                        {
                          listValues.add(hashMap);
                        }
                      }

                    } catch (Exception e) {
                      e.printStackTrace();
                    }

                    final SearchListAdapter adapter = new SearchListAdapter(CreateChannelActivity.this, listValues);
                    listView.setAdapter(adapter);
                    adapter.notifyDataSetChanged();

                    listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                      @RequiresApi(api = Build.VERSION_CODES.N)
                      @Override
                      public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                        HashMap h = new HashMap();
                        h.put("fullname",listValues.get(position).get("fullname"));
                        h.put("id",listValues.get(position).get("id"));
                        h.put("image",listValues.get(position).get("image"));

                        List<String> userIds = getPropertyList(choosen,"id");
                        if (!userIds.contains(listValues.get(position).get("id")))
                        {
                          choosen.add(h);
                          mAdapter.notifyDataSetChanged();
                        }

                        if (choosen.size() == 0) {
                          continue_button.setVisibility(View.GONE);
                        } else {
                          continue_button.setVisibility(View.VISIBLE);
                        }
                      }
                    });
                  }
                }).execute();
              }
            }
          }, 200);
        } else {
          listView.setAdapter(null);
        }
        return true;
      }
    });
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    getMenuInflater().inflate(R.menu.menu_item, menu);
    MenuItem item = menu.findItem(R.id.action_search);
    searchView.setMenuItem(item);
    return true;
  }

  @Override
  protected void attachBaseContext(Context newBase) {
    super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
  }

  public static List<String> getPropertyList(ArrayList<HashMap<String,String>> users, String key)
  {
    List<String> result = new ArrayList<String>();
    for (Map<String,String> map : users) {
      result.add(map.get(key));
    }
    return result;
  }

  public static void listRemover(int position) {
    choosen.remove(position);
    mAdapter.notifyDataSetChanged();
    if (choosen.size() == 0) {
      continue_button.setVisibility(View.GONE);
    }


  }

}
