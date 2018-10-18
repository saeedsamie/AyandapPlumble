package com.morlunk.mumbleclient.app;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.morlunk.mumbleclient.OnTaskCompletedListener;
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

import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;


public class CreateChatFragment extends JumbleServiceFragment implements OnTaskCompletedListener{

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container,
                           Bundle savedInstanceState) {
    View view = inflater.inflate(R.layout.fragment_recent_chats, container, false);



    List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
    nameValuePairs.add(new BasicNameValuePair("func", "4"));
    nameValuePairs.add(new BasicNameValuePair("username", LoginActivity.user_phone_number));

    ServerFetchAsync serverFetchAsync = new ServerFetchAsync(nameValuePairs,this);
    serverFetchAsync.execute();
    JSONObject jsonObject = new JSONObject();
    JSONArray jsonArray = new JSONArray();
    try {
      jsonObject = serverFetchAsync.get();
      jsonArray = jsonObject.getJSONArray("chats");
    } catch (ExecutionException e) {
      e.printStackTrace();
    } catch (InterruptedException e) {
      e.printStackTrace();
    } catch (JSONException e) {
      e.printStackTrace();
    }
    HashMap hashMap = new HashMap();
    final ArrayList<HashMap<String, String>> listValues = new ArrayList<>();
    for (int i=0; i<jsonArray.length();i++){
      hashMap = new HashMap();
      try {

        JSONObject c = jsonArray.getJSONObject(i);

        hashMap.put("UserRole",c.getString("UserRole"));
        hashMap.put("ChatTitle",c.getString("ChatTitle"));
        hashMap.put("ChatId",c.getInt("ChatId"));
        hashMap.put("ChatBio",c.getString("ChatBio"));
        hashMap.put("ChatImage",c.getString("ChatImage"));
        hashMap.put("ChatType",c.getString("ChatType"));
        listValues.add(hashMap);

      } catch (JSONException e) {
        e.printStackTrace();
      }
    }

//    final RecentChatsListAdapter adapter = new RecentChatsListAdapter(getContext(),listValues);
//    ListView listView = view.findViewById(R.id.recent_chats_list);
//    listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
//      @Override
//      public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//
//
//        Chat chat =  new Chat();
//        chat.setId((listValues.get(position).get("ChatId"))+"");
//        chat.setBio(listValues.get(position).get("ChatBio"));
//        chat.setTitle(listValues.get(position).get("ChatTitle"));
//        chat.setImage(listValues.get(position).get("ChatImage"));
//        chat.setImage(listValues.get(position).get("ChatType"));
//        ChatActivity chatActivity = new ChatActivity(chat);
//        Intent intent = new Intent(getContext(),chatActivity.getClass());
//        startActivity(intent);
//      }
//    });
//    listView.setAdapter(adapter);


    return view;

  }


  @Override
  public void onAttach(Context context) {
    super.onAttach(CalligraphyContextWrapper.wrap(context));
  }

  @Override
  public void onDetach() {
    super.onDetach();
  }

  @Override
  public void onTaskCompleted(JSONObject jsonObject) {

  }

  public interface OnFragmentInteractionListener {
    void onFragmentInteraction(Uri uri);
  }
}
