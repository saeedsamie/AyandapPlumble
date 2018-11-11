/*
 * Copyright (C) 2014 Andrew Comminos
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.morlunk.mumbleclient.app;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.PorterDuff;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.jakewharton.picasso.OkHttp3Downloader;
import com.morlunk.mumbleclient.R;
import com.squareup.picasso.Picasso;

import java.io.File;

import jp.wasabeef.picasso.transformations.CropCircleTransformation;
import okhttp3.Cache;
import okhttp3.OkHttpClient;

/**
 * Created by andrew on 01/08/13.
 */
public class DrawerAdapter extends ArrayAdapter<DrawerAdapter.DrawerRow> {
    public static Picasso picassoWithCache;
    // Drawer rows, integer value is id
    public static final int PROFILE_PROFILE = 0;
    public static final int ITEM_SERVER = 1;
    //    public static final int ITEM_PINNED_CHANNELS = 2;
    public static final int ITEM_INFO = 3;
    //    public static final int ITEM_ACCESS_TOKENS = 4;
    public static final int HEADER_SERVERS = 5;
    //    public static final int ITEM_FAVOURITES = 6;
    //    public static final int ITEM_LAN = 7;
//    public static final int ITEM_PUBLIC = 8;
    public static final int HEADER_GENERAL = 9;
    public static final int ITEM_SETTINGS = 10;
    public static final int ITEM_CHAT = 11;
    public static final int ITEM_RECENTS = 12;
    public static final int EXIT = 14;
    public static final int ITEM_CREATE_CHAT = 15;
    public static final int ITEM_CREATE_CHANNEL = 16;
    public static final int ITEM_CREATE_GROUP = 17;

    private static final int HEADER_TYPE = 0;
    private static final int ITEM_TYPE = 1;
    private static final int PROFILE_TYPE = 2;

    private DrawerDataProvider mProvider;

    // TODO clean this up.

    String userid;

    public DrawerAdapter(Context context, DrawerDataProvider provider) {
        super(context, 0);
        mProvider = provider;
        SharedPreferences sharedPreferences = context.getSharedPreferences("MyPref", Context.MODE_PRIVATE);
        String username = sharedPreferences.getString(context.getString(R.string.PREF_TAG_username), "Default Username");
        userid = sharedPreferences.getString(context.getString(R.string.PREF_TAG_userid), "0");
//        add(new DrawerAdapter.DrawerHeader(HEADER_USERNAME, username));

        add(new DrawerAdapter.DrawerProfile(PROFILE_PROFILE, username, userid));
//        add(new DrawerAdapter.DrawerItem(ITEM_CHAT, "chat", R.drawable.ic_action_chat));
        add(new DrawerAdapter.DrawerItem(ITEM_RECENTS, "صفحه ی اصلی", R.drawable.ic_action_favourite_on));
        add(new DrawerAdapter.DrawerItem(ITEM_CREATE_CHAT, "ایجاد گفتگو", R.drawable.ic_action_favourite_on));
//        add(new DrawerAdapter.DrawerItem(ITEM_CREATE_CHANNEL, "ایجاد کانال", R.drawable.channel_c));
        add(new DrawerAdapter.DrawerItem(ITEM_CREATE_GROUP, "ایجاد گروه", R.drawable.ic_action_favourite_on));
//        add(new DrawerAdapter.DrawerItem(ITEM_SERVER, context.getString(R.string.drawer_server), R.drawable.ic_action_channels));
//        add(new DrawerAdapter.DrawerItem(ITEM_PINNED_CHANNELS, context.getString(R.string.drawer_pinned), R.drawable.ic_action_comment));
//        add(new DrawerAdapter.DrawerItem(ITEM_ACCESS_TOKENS, context.getString(R.string.drawer_tokens), R.drawable.ic_action_save));
//        add(new DrawerAdapter.DrawerHeader(HEADER_SERVERS, context.getString(R.string.drawer_header_servers)));
//        add(new DrawerAdapter.DrawerItem(ITEM_FAVOURITES, context.getString(R.string.drawer_favorites), R.drawable.ic_action_favourite_on));
//        add(new DrawerAdapter.DrawerItem(ITEM_LAN, context.getString(R.string.drawer_lan), R.drawable.ic_action_fullscreen)); // Coming soon, TODO
//        add(new DrawerAdapter.DrawerItem(ITEM_PUBLIC, context.getString(R.string.drawer_public), R.drawable.ic_action_search));
//        add(new DrawerAdapter.DrawerHeader(HEADER_GENERAL, context.getString(R.string.general)));
        add(new DrawerAdapter.DrawerItem(ITEM_SETTINGS, context.getString(R.string.action_settings), R.drawable.ic_action_settings));
//        add(new DrawerAdapter.DrawerItem(ITEM_INFO, context.getString(R.string.information), R.drawable.ic_action_info_dark));
        add(new DrawerAdapter.DrawerItem(EXIT, context.getString(R.string.exit), R.drawable.ic_action_expanded));


    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View v = convertView;
        int viewType = getItemViewType(position);
        if (v == null) {
            if (viewType == HEADER_TYPE)
                v = LayoutInflater.from(getContext()).inflate(R.layout.list_drawer_header, parent, false);
            else if (viewType == ITEM_TYPE)
                v = LayoutInflater.from(getContext()).inflate(R.layout.list_drawer_item, parent, false);
            else if (viewType == PROFILE_TYPE) {
                v = LayoutInflater.from(getContext()).inflate(R.layout.list_drawer_profile, parent, false);


            }

        }

        if (viewType == HEADER_TYPE) {
            DrawerHeader header = (DrawerHeader) getItem(position);
            TextView title = (TextView) v.findViewById(R.id.drawer_header_title);
//            switch ((int) getItemId(position)) {
//                case HEADER_CONNECTED_SERVER:
//                    if (mProvider.isConnected()) {
//                        title.setText(mProvider.getConnectedServerName());
//                        break;
//                    }
//                default:
            title.setText(header.title);
//                    break;
//            }
        } else if (viewType == ITEM_TYPE) {
            DrawerItem item = (DrawerItem) getItem(position);
            TextView title = (TextView) v.findViewById(R.id.drawer_item_title);
            ImageView icon = (ImageView) v.findViewById(R.id.drawer_item_icon);
            title.setText(item.title);
            icon.setImageResource(item.icon);

            boolean enabled = isEnabled(position);

            // Set text and icon color+alpha based on enabled/disabled state
            int textColor = title.getCurrentTextColor();
            textColor &= 0x00FFFFFF; // Clear alpha bits
            textColor |= enabled ? 0xFF000000 : 0x55000000; // Set alpha bits depending on whether the state is enabled or disabled
            title.setTextColor(textColor);
            icon.setColorFilter(textColor, PorterDuff.Mode.MULTIPLY);

        } else if (viewType == PROFILE_TYPE) {
            DrawerProfile profile = (DrawerProfile) getItem(position);
            TextView name = (TextView) v.findViewById(R.id.drawer_profile_name);
            final ImageView profile_pic = (ImageView) v.findViewById(R.id.drawer_profile_pic);
            name.setText(profile.title);
//            Glide.with(getContext())
//              .load(LoginActivity.URL+"profile_image/" + userid + ".png")
//              .apply(RequestOptions.circleCropTransform())
//              .into(profile_pic);


//            Picasso.with(getContext())
//              .load(LoginActivity.URL+"profile_image/" + userid + ".png")
//              .networkPolicy(NetworkPolicy.OFFLINE)
//              .into(profile_pic, new Callback() {
//                  @Override
//                  public void onSuccess() {
//
//                  }
//
//                  @Override
//                  public void onError() {
//                      //Try again online if cache failed
//                      Picasso.with(getContext())
//                        .load(LoginActivity.URL+"profile_image/0.png")
//                        .error(R.drawable.ic_action_error)
//                        .into(profile_pic, new Callback() {
//                            @Override
//                            public void onSuccess() {
//
//                            }
//
//                            @Override
//                            public void onError() {
//                                Log.v("Picasso","Could not fetch image");
//                            }
//                        });
//                  }
//              });

            File httpCacheDirectory = new File(getContext().getCacheDir(), "picasso-cache");
            Cache cache = new Cache(httpCacheDirectory, 15 * 1024 * 1024);
            OkHttpClient.Builder okHttpClientBuilder = new OkHttpClient.Builder().cache(cache);
            picassoWithCache = new Picasso.Builder(getContext()).downloader(new OkHttp3Downloader(okHttpClientBuilder.build())).build();
            picassoWithCache.load(LoginActivity.URL+"profile_image/" + userid + ".png").transform(new CropCircleTransformation()).into(profile_pic);




            boolean enabled = isEnabled(position);

            // Set text and profile_pic color+alpha based on enabled/disabled state
            int textColor = name.getCurrentTextColor();
            textColor &= 0x00FFFFFF; // Clear alpha bits
            textColor |= enabled ? 0xFF000000 : 0x55000000; // Set alpha bits depending on whether the state is enabled or disabled
            name.setTextColor(textColor);
//            profile_pic.setColorFilter(textColor, PorterDuff.Mode.MULTIPLY);

        }

        return v;
    }

    @Override
    public long getItemId(int position) {
        return getItem(position).id;
    }

    public DrawerRow getItemWithId(int id) {
        for (int x = 0; x < getCount(); x++) {
            DrawerRow row = getItem(x);
            if (row.id == id) return row;
        }
        return null;
    }

    @Override
    public boolean isEnabled(int position) {
//        int viewType = getItemViewType(position);
//        if (viewType == ITEM_TYPE) {
//            switch ((int) getItemId(position)) {
//                case ITEM_SERVER:
//                case ITEM_INFO:
//                case ITEM_ACCESS_TOKENS:
//                case ITEM_PINNED_CHANNELS:
//                    return mProvider.isConnected();
//                case ITEM_LAN:
//                    return false;
//                default:
//                    return true;
//            }
//        }
        return true; // Default false for headers
    }

    @Override
    public int getItemViewType(int position) {
        Object item = getItem(position);
        if (item instanceof DrawerProfile)
            return PROFILE_TYPE;
        else if (item instanceof DrawerHeader)
            return HEADER_TYPE;
        else if (item instanceof DrawerItem)
            return ITEM_TYPE;
        return ITEM_TYPE;
    }

    @Override
    public int getViewTypeCount() {
        return 3;
    }

    /**
     * Provides context for the drawer module.
     */
    public interface DrawerDataProvider {

        /**
         * @return true if connected, false otherwise.
         */
        public boolean isConnected();

        /**
         * @return The name of the connected server. If not connected, then null.
         */
        public String getConnectedServerName();
    }

    public static class DrawerRow {
        int id;
        String title;

        private DrawerRow(int id, String title) {
            this.id = id;
            this.title = title;
        }
    }

    public static class DrawerHeader extends DrawerRow {

        public DrawerHeader(int id, String title) {
            super(id, title);
        }
    }

    public static class DrawerItem extends DrawerRow {
        int icon;

        public DrawerItem(int id, String title, int icon) {
            super(id, title);
            this.icon = icon;
        }
    }

    public static class DrawerProfile extends DrawerRow {
        String profile_pic_url;

        public DrawerProfile(int id, String title, String userid) {
            super(id, title);
            this.profile_pic_url = userid;
        }
    }
}
