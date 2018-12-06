package com.morlunk.mumbleclient.app;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.miguelcatalan.materialsearchview.MaterialSearchView;
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

import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;


public class CreateGroupActivity extends AppCompatActivity {
  MaterialSearchView searchView;
  ListView lstView;
  List<NameValuePair> nameValuePairs;
  Boolean chatCreated = false;
  private ListView listView;
  private FloatingActionButton continue_button;
  String old;
  private ArrayList<String> members = new ArrayList<>();

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_create_group);
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
      getWindow().getDecorView().setLayoutDirection(View.LAYOUT_DIRECTION_RTL);
    }
    Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
    setSupportActionBar(toolbar);
    getSupportActionBar().setTitle("جستجوی نام کاربری");
    toolbar.setTitleTextColor(Color.parseColor("#FFFFFF"));

    listView = (ListView) findViewById(R.id.create_group_search_listview);

    searchView = (MaterialSearchView) findViewById(R.id.search_view);
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
                                           String toUsers = "";
                                           for (int i = 0 ; i < members.size()-1;i++)
                                           {
                                             toUsers += members.get(i) + ",";
                                           }
                                           toUsers += members.get(members.size()-1);
                                           Intent intent = new Intent(CreateGroupActivity.this,FinishUpGroupActivity.class);
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
                          hashMap.put("selected", "0");

                          listValues.add(hashMap);
                        }

                      } catch (Exception e) {
                        e.printStackTrace();
                      }

                      for (int i = 0; i < listValues.size() - 1; i++) {
                        for (int j = 0; j < members.size() - 1; j++) {
                          if (listValues.get(i).get("id").equals(members.get(j))) {
                            listValues.get(i).put("selected", "1");
                          }
                        }
                      }

                      final SearchListAdapter adapter = new SearchListAdapter(CreateGroupActivity.this, listValues);
                      listView.setAdapter(adapter);
                      adapter.notifyDataSetChanged();

                      listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                          if (listValues.get(position).get("selected").equals("0")) {
                            members.add(listValues.get(position).get("id"));
                            listValues.get(position).put("selected", "1");
                          } else if (listValues.get(position).get("selected").equals("1")) {
                            members.remove(listValues.get(position).get("id"));
                            listValues.get(position).put("selected", "0");
                          }

                          adapter.notifyDataSetChanged();

                          Log.i("MEBERSSSSSS", members.toString());
                          Log.i("LISTTTTTTTT", listValues.toString());

                          if (members.size() == 0) {
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
              }, 500);
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

}
