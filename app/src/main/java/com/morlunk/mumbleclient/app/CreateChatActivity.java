package com.morlunk.mumbleclient.app;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.miguelcatalan.materialsearchview.MaterialSearchView;
import com.morlunk.mumbleclient.OnTaskCompletedListener;
import com.morlunk.mumbleclient.R;
import com.morlunk.mumbleclient.ServerFetchAsync;
import com.morlunk.mumbleclient.service.IPlumbleService;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;


public class CreateChatActivity extends AppCompatActivity {
    MaterialSearchView searchView;
    ListView lstView;
    List<NameValuePair> nameValuePairs;
    Boolean chatCreated = false;
    private ListView listView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_chat);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Material Search");
        toolbar.setTitleTextColor(Color.parseColor("#FFFFFF"));

        listView = (ListView) findViewById(R.id.search_listview);

        searchView = (MaterialSearchView) findViewById(R.id.search_view);

        searchView.setOnSearchViewListener(new MaterialSearchView.SearchViewListener() {
            @Override
            public void onSearchViewShown() {

            }

            @Override
            public void onSearchViewClosed() {

                //If closed Search View , lstView will return default

            }
        });

        searchView.setOnQueryTextListener(new MaterialSearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (newText != null && !newText.isEmpty()) {
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
                                    hashMap.put("image", c.getString("image"));
                                    hashMap.put("id", c.getString("id"));

                                    listValues.add(hashMap);
                                }

                            } catch (Exception e) {
                                e.printStackTrace();
                            }


                            Log.i("WEAKMEWAKM", "" + listValues);

                            SearchListAdapter adapter = new SearchListAdapter(CreateChatActivity.this, listValues);
                            listView.setAdapter(adapter);
                            adapter.notifyDataSetChanged();

                            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                                @Override
                                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                                    nameValuePairs = new ArrayList<NameValuePair>();
                                    nameValuePairs.add(new BasicNameValuePair("func", "createPV"));
                                    String userId = getSharedPreferences("MyPref", Context.MODE_PRIVATE)
                                            .getString(getString(R.string.PREF_TAG_userid), "-2");
                                    nameValuePairs.add(new BasicNameValuePair("fromId", userId));
                                    nameValuePairs.add(new BasicNameValuePair("toId", listValues.get(position).get("id")));
                                    IPlumbleService iPlumbleService = PlumbleActivity.mService;
                                    String chatId = "pv" + new Random().nextInt(1000);
                                    iPlumbleService.getSession()
                                            .createChannel(0, chatId, "Private Chat  " + chatId, 0, true);
                                    nameValuePairs.add(new BasicNameValuePair("chatId", chatId));
                                    final int p = position;
                                    new ServerFetchAsync(nameValuePairs, new OnTaskCompletedListener() {

                                        @Override
                                        public void onTaskCompleted(JSONObject jsonObject) {
                                            try {
                                                chatCreated = jsonObject.getString("PV").equals("CREATED");
                                                if (chatCreated) {
                                                    Intent intent = new Intent(CreateChatActivity.this, ChatActivity.class);
                                                    intent.putExtra("id", (listValues.get(p).get("id")));
                                                    intent.putExtra("bio", (listValues.get(p).get("bio")));
                                                    intent.putExtra("fullname", (listValues.get(p).get("fullname")));
                                                    intent.putExtra("image", (listValues.get(p).get("image")));
                                                    intent.putExtra("type", ("private"));
                                                    startActivity(intent);
                                                } else {
                                                    Toast.makeText(CreateChatActivity.this , "Didn't Create!!", Toast.LENGTH_LONG).show();
                                                }
                                            } catch (Exception e) {
                                                e.printStackTrace();
                                            }
                                        }
                                    }).execute();

                                }
                            });
                        }
                    }).execute();
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
}
