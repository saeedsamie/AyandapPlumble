package com.morlunk.mumbleclient.app;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.jakewharton.picasso.OkHttp3Downloader;
import com.morlunk.mumbleclient.R;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

import jp.wasabeef.picasso.transformations.CropCircleTransformation;
import okhttp3.Cache;
import okhttp3.OkHttpClient;


public class ChatUsersListAdapter extends BaseAdapter {

  public static Picasso picassoWithCache;
  private final ArrayList<HashMap<String, String>> values;
  Context context;

  public ChatUsersListAdapter(Context context, ArrayList<HashMap<String, String>> values) {
    this.context = context;
    this.values = values;
  }

  @Override
  public int getCount() {
    return values.size();
  }

  @Override
  public Object getItem(int i) {
    return i;
  }

  @Override
  public long getItemId(int i) {
    return i;
  }

  //
  @Override
  public View getView(int position, View convertView, ViewGroup parent) {


    ViewHolder viewHolder;

    final View result;

    if (convertView == null) {

      viewHolder = new ViewHolder();
      LayoutInflater inflater = LayoutInflater.from(context);
      convertView = inflater.inflate(R.layout.group_users_row, parent, false);
      viewHolder.chatTitle = (TextView) convertView.findViewById(R.id.chatTitle);
      viewHolder.icon = (ImageView) convertView.findViewById(R.id.appIconIV);

      result = convertView;
      convertView.setTag(viewHolder);
    } else {
      viewHolder = (ViewHolder) convertView.getTag();
      result = convertView;
    }
    viewHolder.chatTitle.setText(values.get(position).get("name"));
    File httpCacheDirectory = new File(context.getCacheDir(), "picasso-cache");
    Cache cache = new Cache(httpCacheDirectory, 15 * 1024 * 1024);
    OkHttpClient.Builder okHttpClientBuilder = new OkHttpClient.Builder().cache(cache);
    picassoWithCache = new Picasso.Builder(context).downloader(new OkHttp3Downloader(okHttpClientBuilder.build())).build();
    picassoWithCache.load(values.get(position).get("image")).transform(new CropCircleTransformation()).into(viewHolder.icon);

    return convertView;

  }

  //
  private static class ViewHolder {

    TextView chatTitle;
    ImageView icon;
  }
}