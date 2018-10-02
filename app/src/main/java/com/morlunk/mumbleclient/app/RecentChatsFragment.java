package com.morlunk.mumbleclient.app;

import android.app.SearchManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.CursorWrapper;
import android.os.Bundle;
import android.os.RemoteException;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;

import com.morlunk.jumble.IJumbleSession;
import com.morlunk.jumble.model.IChannel;
import com.morlunk.jumble.model.IUser;
import com.morlunk.jumble.util.IJumbleObserver;
import com.morlunk.jumble.util.JumbleException;
import com.morlunk.jumble.util.JumbleObserver;
import com.morlunk.mumbleclient.R;
import com.morlunk.mumbleclient.ServerFetchAsync;
import com.morlunk.mumbleclient.Settings;
import com.morlunk.mumbleclient.channel.ChannelListAdapter;
import com.morlunk.mumbleclient.channel.ChannelSearchProvider;
import com.morlunk.mumbleclient.db.PlumbleDatabase;
import com.morlunk.mumbleclient.service.PlumbleService;
import com.morlunk.mumbleclient.util.JumbleServiceFragment;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


public class RecentChatsFragment extends JumbleServiceFragment {

    private PlumbleDatabase mDatabase;
    private PlumbleActivity plumbleActivity;
    private Settings mSettings;
    private ListView listView;
    List<NameValuePair> nameValuePairs;

    private ChannelListAdapter mChannelListAdapter;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private IJumbleObserver mServiceObserver = new JumbleObserver() {
        @Override
        public void onDisconnected(JumbleException e) {
//            mSwipeRefreshLayout.setAdapter(null);
        }

        @Override
        public void onUserJoinedChannel(IUser user, IChannel newChannel, IChannel oldChannel) {
            mChannelListAdapter.updateChannels();
            mChannelListAdapter.notifyDataSetChanged();
            if (getService().isConnected() &&
                    getService().getSession().getSessionId() == user.getSession()) {
                scrollToChannel(newChannel.getId());
            }
        }

        @Override
        public void onChannelAdded(IChannel channel) {
            mChannelListAdapter.updateChannels();
            mChannelListAdapter.notifyDataSetChanged();
        }

        @Override
        public void onChannelRemoved(IChannel channel) {
            mChannelListAdapter.updateChannels();
            mChannelListAdapter.notifyDataSetChanged();
        }

        @Override
        public void onChannelStateUpdated(IChannel channel) {
            mChannelListAdapter.updateChannels();
            mChannelListAdapter.notifyDataSetChanged();
        }

        @Override
        public void onUserConnected(IUser user) {
            mChannelListAdapter.updateChannels();
            mChannelListAdapter.notifyDataSetChanged();
        }

        @Override
        public void onUserRemoved(IUser user, String reason) {
            // If we are the user being removed, don't update the channel list.
            // We won't be in a synchronized state.
            if (!getService().isConnected())
                return;

            mChannelListAdapter.updateChannels();
            mChannelListAdapter.notifyDataSetChanged();
        }

        @Override
        public void onUserStateUpdated(IUser user) {
//            mChannelListAdapter.animateUserStateUpdate(user, mSwipeRefreshLayout);
            getActivity().supportInvalidateOptionsMenu(); // Update self mute/deafen state
        }

        @Override
        public void onUserTalkStateUpdated(IUser user) {
//            mChannelListAdapter.animateUserTalkStateUpdate(user, mSwipeRefreshLayout);
        }
    };
    private BroadcastReceiver mBluetoothReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (getActivity() != null)
                getActivity().supportInvalidateOptionsMenu(); // Update bluetooth menu item
        }
    };

    public void setPlumbleActivity(PlumbleActivity plumbleActivity) {
        this.plumbleActivity = plumbleActivity;
        mDatabase = plumbleActivity.getmDatabase();
        mSettings = plumbleActivity.getmSettings();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        super.onCreate(savedInstanceState);


    }

    private void setupChannelList() throws RemoteException {
//        mChannelListAdapter = new ChannelListAdapter(getActivity(), getService(),
//                mDatabaseProvider.getDatabase(), getChildFragmentManager(),
//                isShowingPinnedChannels(), mSettings.shouldShowUserCount());
//        mChannelListAdapter.setOnChannelClickListener(this);
//        mChannelListAdapter.setOnUserClickListener(this);
//        mSwipeRefreshLayout.setAdapter(mChannelListAdapter);
        mChannelListAdapter.notifyDataSetChanged();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_recent_chats, container, false);
        mSwipeRefreshLayout = view.findViewById(R.id.chat_frame);
        listView = view.findViewById(R.id.recent_chats_list);
//        mSwipeRefreshLayout.setLayoutManager(new LinearLayoutManager(getActivity()));

        SharedPreferences sharedPreferences = getActivity().getSharedPreferences("MyPref", Context.MODE_PRIVATE);
        if (!sharedPreferences.getBoolean("isRegistered", false)) {
            PlumbleService plumbleService = (PlumbleService) getService();
            if (getService() != null && getService().isConnected()) {
                plumbleService.registerUser(getService().getSession().getSessionUser().getSession());
                if (sharedPreferences.edit().putBoolean("isRegistered", true).commit()) {
                    Toast.makeText(this.getContext(), "Registered :D !", Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(this.getContext(), "CAN NOT REGISTER!", Toast.LENGTH_LONG).show();
                }
            }
        }


        nameValuePairs = new ArrayList<NameValuePair>();
        nameValuePairs.add(new BasicNameValuePair("func", "recently"));
//        nameValuePairs.add(new BasicNameValuePair("userId", LoginActivity.user_phone_number));
        new ServerFetchAsync(nameValuePairs, this).execute();
        final RecentChatsFragment recentChatsFragment = this;
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                new ServerFetchAsync(nameValuePairs, recentChatsFragment).execute();
            }

        });
        return view;

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

        MenuItem searchItem = menu.findItem(R.id.menu_search);
        SearchManager searchManager = (SearchManager) getActivity().getSystemService(Context.SEARCH_SERVICE);
        final SearchView searchView = (SearchView) MenuItemCompat.getActionView(searchItem);
        if (searchManager != null) {
            searchView.setSearchableInfo(searchManager.getSearchableInfo(getActivity().getComponentName()));

        }
        searchView.setOnSuggestionListener(new SearchView.OnSuggestionListener() {
            @Override
            public boolean onSuggestionSelect(int i) {
                return false;
            }

            @Override
            public boolean onSuggestionClick(int i) {
                if (getService() == null || !getService().isConnected())
                    return false;
                CursorWrapper cursor = (CursorWrapper) searchView.getSuggestionsAdapter().getItem(i);
                int typeColumn = cursor.getColumnIndex(SearchManager.SUGGEST_COLUMN_INTENT_EXTRA_DATA);
                int dataIdColumn = cursor.getColumnIndex(SearchManager.SUGGEST_COLUMN_INTENT_DATA);
                String itemType = cursor.getString(typeColumn);
                int itemId = cursor.getInt(dataIdColumn);

                IJumbleSession session = getService().getSession();
                if (ChannelSearchProvider.INTENT_DATA_CHANNEL.equals(itemType)) {
                    if (session.getSessionChannel().getId() != itemId) {
                        session.joinChannel(itemId);
                    } else {
                        scrollToChannel(itemId);
                    }
                    return true;
                } else if (ChannelSearchProvider.INTENT_DATA_USER.equals(itemType)) {
                    scrollToUser(itemId);
                    return true;
                }
                return false;
            }
        });

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
            case R.id.menu_search:
                return false;
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

    public void scrollToUser(int userId) {
        int userPosition = mChannelListAdapter.getUserPosition(userId);
//        mSwipeRefreshLayout.smoothScrollToPosition(userPosition);
        //todo
    }

    public void scrollToChannel(int channelId) {
        int channelPosition = mChannelListAdapter.getChannelPosition(channelId);
//        mSwipeRefreshLayout.smoothScrollToPosition(channelPosition);
        //todo
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    public void onTaskExecuted(JSONObject jsonObject) {

        mSwipeRefreshLayout.setRefreshing(false);
        final ArrayList<HashMap<String, String>> listValues = new ArrayList<>();
        try {
            JSONArray jsonArray;
            jsonArray = jsonObject.getJSONArray("recently");

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

    }
}
