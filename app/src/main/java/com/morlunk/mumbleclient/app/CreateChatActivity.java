package com.morlunk.mumbleclient.app;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
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
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;


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
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            getWindow().getDecorView().setLayoutDirection(View.LAYOUT_DIRECTION_RTL);
        }
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("جستجوی نام کاربری");
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
                                    hashMap.put("image", LoginActivity.URL+"profile_image/" + c.getString("image"));
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
                                    String fullName = getSharedPreferences("MyPref", Context.MODE_PRIVATE)
                                            .getString(getString(R.string.PREF_TAG_fullname), "--3");
                                    nameValuePairs.add(new BasicNameValuePair("fromName", fullName));
                                    nameValuePairs.add(new BasicNameValuePair("toName", listValues.get(position).get("fullname")));
                                    nameValuePairs.add(new BasicNameValuePair("fromId", userId));
                                    nameValuePairs.add(new BasicNameValuePair("toId", listValues.get(position).get("id")));
                                    Log.e("channels", PlumbleActivity.iChannels.toString());

                                    final int p = position;
                                    new ServerFetchAsync(nameValuePairs, new OnTaskCompletedListener() {

                                        @Override
                                        public void onTaskCompleted(JSONObject jsonObject) {
                                            IPlumbleService iPlumbleService = PlumbleActivity.mService;
                                            try {
                                                switch (jsonObject.getString("PV")) {
                                                    case "DB ERROR":
                                                        Toast.makeText(CreateChatActivity.this, "DataBase problem Chat didn't create!", Toast.LENGTH_LONG).show();
                                                        break;
                                                    case "CHAT NOT FOUND":
                                                        Toast.makeText(CreateChatActivity.this, "Chat Not found!", Toast.LENGTH_LONG).show();
                                                        break;
                                                    case "CREATED":
                                                        int chatId = Integer.valueOf(jsonObject.getString("chatId"));
                                                        try {
                                                            iPlumbleService.getSession()
                                                                    .createChannel(0, String.valueOf(chatId), "felan", 0, true);//todo delete description
                                                            Log.e("CreateChat", "channel created");
                                                        } catch (Exception e) {
                                                            e.printStackTrace();
                                                            Log.e("CreateChat", "channel Didn't create in jumble request!");
                                                        }
                                                        Intent intent = new Intent(CreateChatActivity.this, ChatActivity.class);
                                                        intent.putExtra("chatId", chatId);
                                                        intent.putExtra("bio", (listValues.get(p).get("bio")));
                                                        intent.putExtra("fullname", (listValues.get(p).get("fullname")));
                                                        intent.putExtra("image", (listValues.get(p).get("image")));
                                                        intent.putExtra("type", ("private"));
                                                        startActivity(intent);
                                                        break;
                                                    case "EXIST":
                                                        Log.e("Ex", "Exist");
                                                        Toast.makeText(CreateChatActivity.this, "Chat Exist", Toast.LENGTH_LONG).show();
                                                        break;
                                                    default:
                                                        Toast.makeText(CreateChatActivity.this, "Didn't Create!!", Toast.LENGTH_LONG).show();
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

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

}
