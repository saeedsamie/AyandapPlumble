package com.morlunk.mumbleclient.app;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
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


public class CreateChatActivity extends AppCompatActivity implements OnTaskCompletedListener {
  MaterialSearchView searchView;
  ListView lstView;
  List<NameValuePair> nameValuePairs;
  private ListView listView;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_create_chat);

    Toolbar toolbar = (Toolbar)findViewById(R.id.toolbar);
    setSupportActionBar(toolbar);
    getSupportActionBar().setTitle("Material Search");
    toolbar.setTitleTextColor(Color.parseColor("#FFFFFF"));

    listView = (ListView)findViewById(R.id.search_listview);

    searchView = (MaterialSearchView)findViewById(R.id.search_view);

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
        if(newText != null && !newText.isEmpty()){
          nameValuePairs = new ArrayList<NameValuePair>();
          nameValuePairs.add(new BasicNameValuePair("func", "search"));
          nameValuePairs.add(new BasicNameValuePair("username", newText));
          new ServerFetchAsync(nameValuePairs, CreateChatActivity.this).execute();
        }
        else
        {

          listView.setAdapter(null);
        }

        return true;
      }

    });




  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    getMenuInflater().inflate(R.menu.menu_item,menu);
    MenuItem item = menu.findItem(R.id.action_search);
    searchView.setMenuItem(item);
    return true;
  }

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


    Log.i("WEAKMEWAKM",""+listValues);


      SearchListAdapter adapter = new SearchListAdapter(this, listValues);
      listView.setAdapter(adapter);
      adapter.notifyDataSetChanged();



  }
}
