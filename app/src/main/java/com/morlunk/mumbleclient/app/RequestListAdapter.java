package com.morlunk.mumbleclient.app;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.morlunk.mumbleclient.R;

import java.util.ArrayList;
import java.util.HashMap;


public class RequestListAdapter extends BaseAdapter {

  private final ArrayList<HashMap<String, String>> values;
  Context context;

  public RequestListAdapter(Context context, ArrayList<HashMap<String, String>> values) {
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
      convertView = inflater.inflate(R.layout.requests_row, parent, false);
      viewHolder.request_fullname = convertView.findViewById(R.id.request_fullname);
      viewHolder.request_username = convertView.findViewById(R.id.request_username);

      result = convertView;
      convertView.setTag(viewHolder);
    } else {
      viewHolder = (ViewHolder) convertView.getTag();
      result = convertView;
    }
    viewHolder.request_fullname.setText(values.get(position).get("fullname"));
    viewHolder.request_username.setText(values.get(position).get("username"));
    return convertView;
  }

  private static class ViewHolder {

    TextView request_fullname;
    TextView request_username;
    ImageView icon;
  }

}