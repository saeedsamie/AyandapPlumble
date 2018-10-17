package com.morlunk.mumbleclient.app;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import com.morlunk.jumble.IJumbleSession;
import com.morlunk.jumble.model.IUser;
import com.morlunk.mumbleclient.OnTaskCompletedListener;
import com.morlunk.mumbleclient.R;
import com.morlunk.mumbleclient.ServerFetchAsync;
import com.morlunk.mumbleclient.util.JumbleServiceFragment;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static android.content.Context.MODE_PRIVATE;


public class RecentChatsFragment extends JumbleServiceFragment {

    List<NameValuePair> nameValuePairs;
    Boolean permission = false;
    private ListView listView;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private RecentChatsFragment context;
    private String userId;
    String fullName;

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        permission = false;
        setHasOptionsMenu(true);
        super.onCreate(savedInstanceState);
        permission = true;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_recent_chats, container, false);
        mSwipeRefreshLayout = view.findViewById(R.id.chat_frame);
        userId = getActivity().getSharedPreferences("MyPref", MODE_PRIVATE).getString(getString(R.string.PREF_TAG_userid), "-2");
        listView = view.findViewById(R.id.recent_chats_list);

//        mSwipeRefreshLayout.setLayoutManager(new LinearLayoutManager(getActivity()));

        SharedPreferences sharedPreferences = getActivity().getSharedPreferences("MyPref", MODE_PRIVATE);
        fullName = sharedPreferences.getString(getString(R.string.PREF_TAG_fullname), "username");

//        if (!sharedPreferences.getBoolean("isRegistered", false)) {
//            PlumbleService plumbleService = (PlumbleService) getService();
//            if (getService() != null && getService().isConnected()) {
//                plumbleService.registerUser(getService().getSession().getSessionUser().getSession());
//                if (sharedPreferences.edit().putBoolean("isRegistered", true).commit()) {
//                    Toast.makeText(this.getContext(), "Registered :D !", Toast.LENGTH_LONG).show();
//                } else {
//                    Toast.makeText(this.getContext(), "CAN NOT REGISTER!", Toast.LENGTH_LONG).show();
//                }
//            }
//        }
        mSwipeRefreshLayout.setRefreshing(true);
        updateListView();
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                updateListView();
            }
        });
        context = this;
        return view;
    }

    private void updateListView() {
        nameValuePairs = new ArrayList<NameValuePair>();
        nameValuePairs.add(new BasicNameValuePair("func", "recently"));
        nameValuePairs.add(new BasicNameValuePair("userId", userId));
        new ServerFetchAsync(nameValuePairs, new OnTaskCompletedListener() {
            @Override
            public void onTaskCompleted(JSONObject jsonObject) {
                if (permission) {
                    mSwipeRefreshLayout.setRefreshing(false);
                    final ArrayList<HashMap<String, String>> listValues = new ArrayList<>();
                    try {
                        JSONArray jsonArray;
                        jsonArray = jsonObject.getJSONArray("recently");

                        for (int i = 0; i < jsonArray.length(); i++) {
                            HashMap hashMap = new HashMap();
                            JSONObject c = jsonArray.getJSONObject(i);

                            hashMap.put("role", c.getString("role"));
                            hashMap.put("title", c.getString("title"));
                            hashMap.put("type", c.getString("type"));
                            hashMap.put("id", c.getString("id"));//chatId
                            hashMap.put("image", c.getString("image"));
                            hashMap.put("bio", c.getString("bio"));
                            if (c.getString("type").equals("pv")) {
                                String[] strings = c.getString("title").split(",");
                                String[] ids = c.getString("bio").toString().split(",");
                                if (strings[0].equals(fullName)){
                                    hashMap.put("title", strings[1]);
                                }else {
                                    hashMap.put("title", strings[0]);
                                }
                                if (ids[0].equals(userId)){
                                    hashMap.put("id", ids[1]);
                                    hashMap.put("image", "http://192.168.2.18/SqliteTest/profile_image/"+ids[1]+".png");
                                }else {
                                    hashMap.put("id", ids[0]);
                                    hashMap.put("image", "http://192.168.2.18/SqliteTest/profile_image/"+ids[0]+".png");
                                }

                            }
                            listValues.add(hashMap);
                            Log.i("HBJFDHB",listValues.toString());
                        }

                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    RecentChatsListAdapter adapter = new RecentChatsListAdapter(getContext(), listValues);
                    listView.setAdapter(adapter);
                    listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                            Intent intent = new Intent(getContext(), ChatActivity.class);
                            HashMap<String, String> map = listValues.get(position);
                            Log.e("map", "map" + map.toString());
                            intent.putExtra("id", (listValues.get(position).get("id")));
                            intent.putExtra("bio", (listValues.get(position).get("bio")));
                            intent.putExtra("fullname", (listValues.get(position).get("title")));
                            intent.putExtra("image", (listValues.get(position).get("image")));
                            intent.putExtra("type", (listValues.get(position).get("type")));
                            try {
                                PlumbleActivity.mService.getSession().joinChannel(Integer.valueOf(listValues.get(position).get("id")));
                            } catch (Exception e) {
                                e.printStackTrace();
                                //to do Create channel !
                            }

                            context.startActivity(intent);


                        }
                    });
                }
            }
        }).execute();
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        registerForContextMenu(mSwipeRefreshLayout);
//        getActivity().registerReceiver(mBluetoothReceiver, new IntentFilter(AudioManager.ACTION_SCO_AUDIO_STATE_CHANGED));
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);

//        MenuItem muteItem = menu.findItem(R.id.menu_mute_button);
//        MenuItem deafenItem = menu.findItem(R.id.menu_deafen_button);

        if (getService() != null && getService().isConnected()) {
            IJumbleSession session = getService().getSession();

            // Color the action bar icons to the primary text color of the theme, TODO move this elsewhere
            int foregroundColor = getActivity().getTheme().obtainStyledAttributes(new int[]{android.R.attr.textColorPrimaryInverse}).getColor(0, -1);

            IUser self = session.getSessionUser();
//            muteItem.setIcon(self.isSelfMuted() ? R.drawable.ic_action_microphone_muted : R.drawable.ic_action_microphone);
//            deafenItem.setIcon(self.isSelfDeafened() ? R.drawable.ic_action_audio_muted : R.drawable.ic_action_audio);
//            muteItem.getIcon().mutate().setColorFilter(foregroundColor, PorterDuff.Mode.MULTIPLY);
//            deafenItem.getIcon().mutate().setColorFilter(foregroundColor, PorterDuff.Mode.MULTIPLY);

//            MenuItem bluetoothItem = menu.findItem(R.id.menu_bluetooth);
//            bluetoothItem.setChecked(session.usingBluetoothSco());
        }
    }


    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.fragment_channel_list, menu);

//        MenuItem searchItem = menu.findItem(R.id.menu_requests);
//        SearchManager searchManager = (SearchManager) getActivity().getSystemService(Context.SEARCH_SERVICE);
//        final SearchView searchView = (SearchView) MenuItemCompat.getActionView(searchItem);
//        if (searchManager != null) {
//            searchView.setSearchableInfo(searchManager.getSearchableInfo(getActivity().getComponentName()));
//
//        }
//        searchView.setOnSuggestionListener(new SearchView.OnSuggestionListener() {
//            @Override
//            public boolean onSuggestionSelect(int i) {
//                return false;
//            }
//            @Override
//            public boolean onSuggestionClick(int i) {
//                if (getService() == null || !getService().isConnected())
//                    return false;
//                CursorWrapper cursor = (CursorWrapper) searchView.getSuggestionsAdapter().getItem(i);
//                int typeColumn = cursor.getColumnIndex(SearchManager.SUGGEST_COLUMN_INTENT_EXTRA_DATA);
//                int dataIdColumn = cursor.getColumnIndex(SearchManager.SUGGEST_COLUMN_INTENT_DATA);
//                String itemType = cursor.getString(typeColumn);
//                int itemId = cursor.getInt(dataIdColumn);

//                IJumbleSession session = getService().getSession();
//                if (ChannelSearchProvider.INTENT_DATA_CHANNEL.equals(itemType)) {
//                    if (session.getSessionChannel().getId() != itemId) {
//                        session.joinChannel(itemId);
//                    } else {
//                        scrollToChannel(itemId);
//                    }
//                    return true;
//                } else if (ChannelSearchProvider.INTENT_DATA_USER.equals(itemType)) {
//                    scrollToUser(itemId);
//                    return true;
//                }
//                return false;
//            }
//        });

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (getService() == null || !getService().isConnected())
            return super.onOptionsItemSelected(item);

        IJumbleSession session = getService().getSession();
        switch (item.getItemId()) {
//            case R.id.menu_mute_button: {
//                IUser self = session.getSessionUser();
//
//                boolean muted = !self.isSelfMuted();
//                boolean deafened = self.isSelfDeafened();
//                deafened &= muted; // Undeafen if mute is off
//                session.setSelfMuteDeafState(muted, deafened);
//
//                getActivity().supportInvalidateOptionsMenu();
//                return true;
//            }
//            case R.id.menu_deafen_button: {
//                IUser self = session.getSessionUser();
//
//                boolean deafened = !self.isSelfDeafened();
//                session.setSelfMuteDeafState(deafened, deafened);
//
//                getActivity().supportInvalidateOptionsMenu();
//                return true;
//            }
//            case R.id.menu_requests:
//                return false;
//            case R.id.menu_bluetooth:
//                item.setChecked(!item.isChecked());
//                if (item.isChecked()) {
//                    session.enableBluetoothSco();
//                } else {
//                    session.disableBluetoothSco();
//                }
//                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }
}
