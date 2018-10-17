package com.morlunk.mumbleclient.app;

import android.app.AlertDialog;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.morlunk.mumbleclient.OnTaskCompletedListener;
import com.morlunk.mumbleclient.R;
import com.morlunk.mumbleclient.ServerFetchAsync;
import com.wdullaer.swipeactionadapter.SwipeActionAdapter;
import com.wdullaer.swipeactionadapter.SwipeDirection;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static com.wdullaer.swipeactionadapter.SwipeDirection.DIRECTION_FAR_LEFT;
import static com.wdullaer.swipeactionadapter.SwipeDirection.DIRECTION_FAR_RIGHT;
import static com.wdullaer.swipeactionadapter.SwipeDirection.DIRECTION_NORMAL_LEFT;
import static com.wdullaer.swipeactionadapter.SwipeDirection.DIRECTION_NORMAL_RIGHT;

public class RequestActivity extends AppCompatActivity {

    ArrayList<NameValuePair> nameValuePairs;
    String userId;
    private ListView requests_list;
    private ArrayList<HashMap<String, String>> listValues;
    private SwipeRefreshLayout mSwipeRefreshLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.popup_requests);
        mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.request_panel_swipeRefreshLayout);
        nameValuePairs = new ArrayList<NameValuePair>();
        userId = getSharedPreferences("MyPref", MODE_PRIVATE).getString(getString(R.string.PREF_TAG_userid), "-2");

        nameValuePairs.add(new BasicNameValuePair("func", "requests"));
        nameValuePairs.add(new BasicNameValuePair("userId", userId));
        mSwipeRefreshLayout.setRefreshing(true);

        updateListView();
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                updateListView();
            }

        });


        requests_list = (ListView) findViewById(R.id.requests_list);
        requests_list.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                AlertDialog.Builder builder = new AlertDialog.Builder(RequestActivity.this);
                List<NameValuePair> value = new ArrayList<>();
                value.add(new BasicNameValuePair("func", "acceptPV"));
                value.add(new BasicNameValuePair("userId", userId));
                value.add(new BasicNameValuePair("chatId", listValues.get(position).get("chatId")));

                builder.setMessage("گفتگو پذیرفته شد").create().show();
                new ServerFetchAsync(value, new OnTaskCompletedListener() {
                    @Override
                    public void onTaskCompleted(JSONObject jsonObject) {
                        AlertDialog.Builder builder = new AlertDialog.Builder(RequestActivity.this);
                        builder.setMessage(jsonObject.toString()).create().show();
                    }
                }).execute();
            }
        });
    }

    //This method defined for reduce repeating following lines..
    // @Saeed
    private void updateListView() {
        new ServerFetchAsync(nameValuePairs, new OnTaskCompletedListener() {
            @Override
            public void onTaskCompleted(JSONObject jsonObject) {
                Log.i("requests", jsonObject.toString());
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
                        Log.i("test", "test");
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }


                RequestListAdapter adapter = new RequestListAdapter(RequestActivity.this, listValues);
                final SwipeActionAdapter swipeActionAdapter = new SwipeActionAdapter(adapter);
                swipeActionAdapter.setListView(requests_list);
                requests_list.setAdapter(swipeActionAdapter);
                adapter.notifyDataSetChanged();
                swipeActionAdapter.addBackground(DIRECTION_FAR_LEFT, R.layout.row_bg_left_far)
                        .addBackground(DIRECTION_NORMAL_LEFT, R.layout.row_bg_left)
                        .addBackground(DIRECTION_FAR_RIGHT, R.layout.row_bg_right_far)
                        .addBackground(DIRECTION_NORMAL_RIGHT, R.layout.row_bg_right);
                swipeActionAdapter.setSwipeActionListener(new SwipeActionAdapter.SwipeActionListener() {
                    @Override
                    public boolean hasActions(int position, SwipeDirection direction) {
                        if (direction.isLeft())
                            return true; // Change this to false to disable left swipes
                        if (direction.isRight()) return true;
                        return false;
                    }

                    @Override
                    public boolean shouldDismiss(int position, SwipeDirection direction) {
                        // Only dismiss an item when swiping normal left
                        return direction == SwipeDirection.DIRECTION_NORMAL_LEFT;
                    }

                    @Override
                    public void onSwipe(int[] pos, SwipeDirection[] dirct) {
                        for (int i = 0; i < pos.length; i++) {
                            SwipeDirection direction = dirct[i];
                            int position = pos[i];
                            String dir = "";
                            List<NameValuePair> value = new ArrayList<>();
                            AlertDialog.Builder builder = new AlertDialog.Builder(RequestActivity.this);

                            switch (direction) {
                                case DIRECTION_FAR_LEFT:
                                    dir = "Far left";
                                    value.add(new BasicNameValuePair("func", "declinePV"));
                                    value.add(new BasicNameValuePair("userId", userId));
                                    value.add(new BasicNameValuePair("chatId", listValues.get(position).get("chatId")));
                                    builder.setMessage("گفتگو پذیرفته نشد").create().show();
                                    break;
                                case DIRECTION_NORMAL_LEFT:
                                    dir = "Left";
                                    value.add(new BasicNameValuePair("func", "declinePV"));
                                    value.add(new BasicNameValuePair("userId", userId));
                                    value.add(new BasicNameValuePair("chatId", listValues.get(position).get("chatId")));
                                    builder.setMessage("گفتگو پذیرفته نشد").create().show();

                                    break;
                                case DIRECTION_FAR_RIGHT:
                                    dir = "Far right";
                                    value.add(new BasicNameValuePair("func", "acceptPV"));
                                    value.add(new BasicNameValuePair("userId", userId));
                                    value.add(new BasicNameValuePair("chatId", listValues.get(position).get("chatId")));
                                    builder.setMessage("گفتگو پذیرفته شد").create().show();
                                    break;
                                case DIRECTION_NORMAL_RIGHT:
                                    value.add(new BasicNameValuePair("func", "acceptPV"));
                                    value.add(new BasicNameValuePair("userId", userId));
                                    value.add(new BasicNameValuePair("chatId", listValues.get(position).get("chatId")));
                                    builder.setMessage("گفتگو پذیرفته شد").create().show();
                                    dir = "Right";
                                    break;
                            }
                            new ServerFetchAsync(value, new OnTaskCompletedListener() {
                                @Override
                                public void onTaskCompleted(JSONObject jsonObject) {
                                    AlertDialog.Builder builder = new AlertDialog.Builder(RequestActivity.this);
                                    try {
                                        builder.setMessage(jsonObject.getString("request")).create().show();
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                }
                            }).execute();
                            updateListView();
                            swipeActionAdapter.notifyDataSetChanged();
                        }
                    }
                });
                mSwipeRefreshLayout.setRefreshing(false);
            }
        }).execute();
    }
}
