package com.morlunk.ayandap.app;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.Snackbar;
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

import com.morlunk.ayandap.OnTaskCompletedListener;
import com.morlunk.ayandap.R;
import com.morlunk.ayandap.ServerFetchAsync;
import com.morlunk.ayandap.channel.ChannelListAdapter;
import com.morlunk.ayandap.util.JumbleServiceFragment;
import com.morlunk.jumble.IJumbleSession;
import com.morlunk.jumble.model.IChannel;
import com.morlunk.jumble.model.IUser;
import com.morlunk.jumble.util.JumbleObserver;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static android.content.Context.MODE_PRIVATE;
import static com.morlunk.ayandap.app.PlumbleActivity.iChannels;


public class RecentChatsFragment extends JumbleServiceFragment {

    List<NameValuePair> nameValuePairs;
    Boolean permission = false;
    String fullName;
    private ListView listView;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private String userId;
    private JumbleObserver mObserver = new JumbleObserver() {

        @Override
        public void onChannelAdded(IChannel channel) {
            if (PlumbleActivity.mService.isConnected()) {
//                updateListView();
//                Log.e("channel", "addded" + iChannels.toString());
                super.onChannelAdded(channel);
            }
        }

        @Override
        public void onChannelRemoved(IChannel channel) {
            if (PlumbleActivity.mService.isConnected()) {
//                updateListView();
//                Log.e("channels", "removed" + iChannels.toString());
                super.onChannelRemoved(channel);
            }
        }
    };
    private ArrayList<IChannel> chls;

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
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_recent_chats, container, false);
        mSwipeRefreshLayout = view.findViewById(R.id.chat_frame);
        userId = getActivity().getSharedPreferences("MyPref", MODE_PRIVATE).getString(getString(R.string.PREF_TAG_userid), "-2");
        listView = view.findViewById(R.id.recent_chats_list);

//        mSwipeRefreshLayout.setLayoutManager(new LinearLayoutManager(getActivity()));

        SharedPreferences sharedPreferences = getActivity().getSharedPreferences("MyPref", MODE_PRIVATE);
        fullName = sharedPreferences.getString(getString(R.string.PREF_TAG_fullname), "username");


        mSwipeRefreshLayout.setRefreshing(true);
        updateListView();
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                updateListView();
                Log.i("server_channels", ChannelListAdapter.server_channels.toString());
            }
        });
        try {
            if (PlumbleActivity.mService.isConnected()) {
                PlumbleActivity.mService.registerObserver(mObserver);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
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

                            hashMap.put("access", c.getString("access"));
                            hashMap.put("property", c.getString("property"));
                            hashMap.put("title", c.getString("title"));
                            hashMap.put("type", c.getString("type"));
                            hashMap.put("image", LoginActivity.URL + "profile_image/" + c.getString("image"));
                            hashMap.put("chatId", c.getString("id"));//chatId
                            hashMap.put("bio", c.getString("bio"));
                            if (c.getString("type").equals("pv")) {
                                String[] strings = c.getString("title").split(",");
                                String[] ids = c.getString("bio").split(",");
                                if (strings[0].equals(fullName)) {
                                    hashMap.put("title", strings[1]);
                                } else {
                                    hashMap.put("title", strings[0]);
                                }
                                if (ids[0].equals(userId)) {
                                    hashMap.put("id", ids[1]);
                                    hashMap.put("image", LoginActivity.URL + "profile_image/" + ids[1] + ".png");
                                } else {
                                    hashMap.put("image", LoginActivity.URL + "profile_image/" + ids[0] + ".png");
                                    hashMap.put("id", ids[0]);
                                }

                            }
                            listValues.add(hashMap);
//                            Log.i("HBJFDHB", listValues.toString());
                        }

                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    RecentChatsListAdapter adapter = new RecentChatsListAdapter(getContext(), listValues);
                    listView.setAdapter(adapter);
                    listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                            final ProgressDialog progressDialog = new ProgressDialog(getActivity());
                            progressDialog.setIndeterminate(true);
                            progressDialog.setCancelable(true);

                            progressDialog.setMessage("لطفا صبر کنید..");

                            progressDialog.show();

                            final Intent intent = new Intent(getContext(), ChatActivity.class);
                            HashMap<String, String> map = listValues.get(position);
                            Log.e("map", "map" + map.toString());
                            final int chatId = Integer.valueOf(listValues.get(position).get("chatId"));
                            intent.putExtra("chatId", listValues.get(position).get("chatId"));
                            Log.e("chatId", "recentChats:" + chatId);
                            intent.putExtra("bio", (listValues.get(position).get("bio")));
                            intent.putExtra("fullname", (listValues.get(position).get("title")));
                            intent.putExtra("image", (listValues.get(position).get("image")));
                            intent.putExtra("type", (listValues.get(position).get("type")));
                            intent.putExtra("property", (listValues.get(position).get("property")));
                            intent.putExtra("access", (listValues.get(position).get("access")));
                            ArrayList<IChannel> channels = iChannels;
                            int channelId = -2;
synchronized (this) {
    for (int i = 0; i < channels.size(); i++) {
        channels = iChannels;
        if (channels.get(i).getName().trim().equals(String.valueOf(chatId).trim())) {
            channelId = channels.get(i).getId();
            break;
        }
        channels = iChannels;
        Log.i("channels", channels.get(i).getName());
    }
}
                            if (channelId == -2 && PlumbleActivity.mService.getSession() != null) {
                                try {
//                                    final Handler handler = new Handler();
//                                    handler.postDelayed(new Runnable() {
//                                        @Override
//                                        synchronized public void run() {
                                            PlumbleActivity.mService.getSession().createChannel(0, String.valueOf(chatId), "", 0, true);
//                                            handler.postDelayed(this, 100);
//                                            if (isChannelCreated(chatId)) {
//                                                handler.removeCallbacksAndMessages(null);
                                                progressDialog.hide();
                                                getActivity().startActivity(intent);
//                                            }
//                                        }
//                                    }, 100);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                    progressDialog.hide();
                                }
                            } else if (channelId != -2 && PlumbleActivity.mService.getSession() != null) {
                                try {
                                    final int finalChannelId = channelId;
                                    final Handler handler = new Handler();
                                    Runnable runnable = new Runnable() {
                                        @Override
                                        public void run() {
                                            PlumbleActivity.mService.getSession().joinChannel(finalChannelId);
                                            handler.postDelayed(this, 1500);
                                            if (isUserJoined(finalChannelId)) {
                                                handler.removeCallbacksAndMessages(null);
                                                progressDialog.hide();
                                                getActivity().startActivity(intent);
                                            }
                                        }
                                    };
                                    handler.postDelayed(runnable, 1500);
                                    progressDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
                                        @Override
                                        public void onCancel(DialogInterface dialog) {
                                            handler.removeCallbacksAndMessages(null);
                                        }
                                    });
                                } catch (Exception e) {
                                    e.printStackTrace();
                                    progressDialog.hide();
                                }
                            } else {
                                progressDialog.hide();
                            }
                        }
                    });

                    listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
                        @Override
                        public boolean onItemLongClick(AdapterView<?> arg0, final View arg1,
                                                       final int pos, long id) {

                            if (listValues.get(pos).get("type").equals("pv")) {
                                Snackbar.make(arg1, "آیا از حذف این چت اطمینان دارید ؟", Snackbar.LENGTH_LONG)
                                        .setAction("بلی", new View.OnClickListener() {
                                            @Override
                                            public void onClick(View v) {

                                                nameValuePairs = new ArrayList<NameValuePair>();
                                                nameValuePairs.add(new BasicNameValuePair("func", "removePV"));
                                                nameValuePairs.add(new BasicNameValuePair("chatId", listValues.get(pos).get("chatId")));

                                                new ServerFetchAsync(nameValuePairs, new OnTaskCompletedListener() {
                                                    @Override
                                                    public void onTaskCompleted(JSONObject jsonObject) {
                                                        try {
                                                            if (jsonObject.getString("PV").equals("removed")) {

                                                                Snackbar.make(arg1, "چت مورد نظر حذف شد", Snackbar.LENGTH_SHORT)
                                                                        .show();
                                                                updateListView();
                                                            } else {
                                                                Snackbar.make(arg1, "خطایی پیش آمده ، دوباره امتحان کنید", Snackbar.LENGTH_SHORT)
                                                                        .show();
                                                            }

                                                        } catch (JSONException e1) {
                                                            e1.printStackTrace();
                                                        }
                                                    }
                                                }).execute();
                                            }
                                        }).show();
                            }



                            if (listValues.get(pos).get("type").equals("group")) {
                                Snackbar.make(arg1, "آیا میخواهید از این گروه خارج شوید ؟", Snackbar.LENGTH_LONG)
                                  .setAction("بلی", new View.OnClickListener() {
                                      @Override
                                      public void onClick(View v) {

                                          nameValuePairs = new ArrayList<NameValuePair>();
                                          nameValuePairs.add(new BasicNameValuePair("func", "leaveGroup"));
                                          nameValuePairs.add(new BasicNameValuePair("chatId", listValues.get(pos).get("chatId")));
                                          nameValuePairs.add(new BasicNameValuePair("userId",  getActivity().getSharedPreferences("MyPref", MODE_PRIVATE).getString(getString(R.string.PREF_TAG_userid), "-2")));

                                          new ServerFetchAsync(nameValuePairs, new OnTaskCompletedListener() {
                                              @Override
                                              public void onTaskCompleted(JSONObject jsonObject) {
                                                  try {
                                                      if (jsonObject.getString("GROUP").equals("left")) {

                                                          Snackbar.make(arg1, "شما گروه را ترک کردید", Snackbar.LENGTH_SHORT)
                                                            .show();
                                                          updateListView();
                                                      } else {
                                                          Snackbar.make(arg1, "خطایی پیش آمده ، دوباره امتحان کنید", Snackbar.LENGTH_SHORT)
                                                            .show();
                                                      }

                                                  } catch (JSONException e1) {
                                                      e1.printStackTrace();
                                                  }
                                              }
                                          }).execute();
                                      }
                                  }).show();
                            }

                            if (listValues.get(pos).get("type").equals("channel")) {
                                Snackbar.make(arg1, "آیا میخواهید از این کانال خارج شوید ؟", Snackbar.LENGTH_LONG)
                                  .setAction("بلی", new View.OnClickListener() {
                                      @Override
                                      public void onClick(View v) {

                                          nameValuePairs = new ArrayList<NameValuePair>();
                                          nameValuePairs.add(new BasicNameValuePair("func", "leaveChannel"));
                                          nameValuePairs.add(new BasicNameValuePair("chatId", listValues.get(pos).get("chatId")));
                                          nameValuePairs.add(new BasicNameValuePair("userId",  getActivity().getSharedPreferences("MyPref", MODE_PRIVATE).getString(getString(R.string.PREF_TAG_userid), "-2")));

                                          new ServerFetchAsync(nameValuePairs, new OnTaskCompletedListener() {
                                              @Override
                                              public void onTaskCompleted(JSONObject jsonObject) {
                                                  try {
                                                      if (jsonObject.getString("CHANNEL").equals("left")) {

                                                          Snackbar.make(arg1, "شما کانال را ترک کردید", Snackbar.LENGTH_SHORT)
                                                            .show();
                                                          updateListView();
                                                      } else {
                                                          Snackbar.make(arg1, "خطایی پیش آمده ، دوباره امتحان کنید", Snackbar.LENGTH_SHORT)
                                                            .show();
                                                      }

                                                  } catch (JSONException e1) {
                                                      e1.printStackTrace();
                                                  }
                                              }
                                          }).execute();
                                      }
                                  }).show();
                            }




                            return true;
                        }
                    });
                }
            }
        }).execute();
    }

    synchronized private boolean isChannelCreated(int chatId) {
        updateChannelLists();
        ArrayList<IChannel> channels = chls;
        for (int i = 0; i < channels.size(); i++) {
            if (channels.get(i).getName().equals(String.valueOf(chatId))) {
                return true;
            }
        }
        return false;
    }

    private boolean isUserJoined(int channelId) { //todo does not working completely
        updateChannelLists();
        ArrayList<IChannel> channels = chls;
        for (int i = 0; i < channels.size(); i++) {
            if (channels.get(i).getId() == channelId) {
                List<? extends IUser> users = channels.get(i).getUsers();
                for (int j = 0; j < users.size(); j++) {
                    if (users.get(j).getName().trim().split("|")[0].equals(PlumbleActivity.plumbleUserName.split("|")[0])) {
                        Log.e("heeeeeeeeellllllll", "heeelloooo  -------- i = " + i);
                        return true;
                    }
                }
            }
        }
        return false;
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
            case R.id.menu_requests:
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

    @Override
    public void onDetach() {
        super.onDetach();
    }

    ArrayList<IChannel> updateChannelLists() {

        chls = new ArrayList<IChannel>();
        if (chls.size() == 0) {
            chls.add(PlumbleActivity.mService.getSession().getChannel(0));
        }
        for (int i = 0; i < chls.size(); i++) {
            IChannel channel = PlumbleActivity.mService.getSession().getChannel(chls.get(i).getId());
            if (channel != null) {
//                constructNodes(null, channel, 0, mNodes);
                iChannels.add(channel);
                chls.addAll(channel.getSubchannels());
            }
        }
        return iChannels;
    }
}
