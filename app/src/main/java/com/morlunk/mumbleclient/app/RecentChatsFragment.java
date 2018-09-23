package com.morlunk.mumbleclient.app;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import com.morlunk.mumbleclient.R;
import com.morlunk.mumbleclient.ServerFetchAsync;
import com.morlunk.mumbleclient.util.JumbleServiceFragment;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;


public class RecentChatsFragment extends JumbleServiceFragment {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_recent_chats, container, false);
        final ArrayList<HashMap<String, String>> listValues = new ArrayList<>();

        List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
        nameValuePairs.add(new BasicNameValuePair("func", "2"));
        nameValuePairs.add(new BasicNameValuePair("phone", LoginActivity.user_phone));

        ServerFetchAsync serverFetchAsync = new ServerFetchAsync(nameValuePairs);



        try {
            JSONObject jsonObject;
            jsonObject = serverFetchAsync.getJsonResponse();
            JSONArray jsonArray;
            jsonArray = jsonObject.getJSONArray("chats");

            for (int i = 0; i < jsonArray.length(); i++) {
                HashMap hashMap = new HashMap();
                JSONObject c = jsonArray.getJSONObject(i);

                hashMap.put("UserRole", c.getString("UserRole"));
                hashMap.put("ChatTitle", c.getString("ChatTitle"));
                hashMap.put("ChatId", c.getString("ChatId"));
                hashMap.put("ChatBio", c.getString("ChatBio"));
                hashMap.put("ChatImage", c.getString("ChatImage"));
                hashMap.put("ChatType", c.getString("ChatType"));
                listValues.add(hashMap);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }


        if (listValues.size() > 0) {
            RecentChatsListAdapter adapter = new RecentChatsListAdapter(getContext(), listValues);
            ListView listView = view.findViewById(R.id.recent_chats_list);
            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                    Intent intent = new Intent(getContext(), ChatActivity.class);

                    HashMap<String, String> map = listValues.get(position);
                    Log.e("map", "map" + map.toString());

                    intent.putExtra("ChatId", (listValues.get(position).get("ChatId")));
                    intent.putExtra("ChatBio", (listValues.get(position).get("ChatBio")));
                    intent.putExtra("ChatTitle", (listValues.get(position).get("ChatTitle")));
                    intent.putExtra("ChatImage", (listValues.get(position).get("ChatImage")));
                    intent.putExtra("ChatType", (listValues.get(position).get("ChatType")));

                    startActivity(intent);
                }
            });
            listView.setAdapter(adapter);
        }


        return view;

    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }
}
