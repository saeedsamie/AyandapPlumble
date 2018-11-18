package com.morlunk.mumbleclient.app;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Shader;
import android.os.AsyncTask;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.morlunk.mumbleclient.R;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;

import jp.wasabeef.picasso.transformations.CropCircleTransformation;

public class SearchListAdapter extends BaseAdapter {
    private final ArrayList<HashMap<String, String>> values;
    Context context;

    public SearchListAdapter(Context context, ArrayList<HashMap<String, String>> values) {
        //super(context, R.layout.single_list_app_item, utilsArrayList);
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


        final ViewHolder viewHolder;

        final View result;

        if (convertView == null) {

            viewHolder = new ViewHolder();
            LayoutInflater inflater = LayoutInflater.from(context);
            convertView = inflater.inflate(R.layout.search_row, parent, false);
            viewHolder.userFullname = (TextView) convertView.findViewById(R.id.userFullname);
            viewHolder.username = (TextView) convertView.findViewById(R.id.username);
            viewHolder.icon = (ImageView) convertView.findViewById(R.id.appIconIV);
            viewHolder.search_layout = (LinearLayout) convertView.findViewById(R.id.search_layout);

            result = convertView;
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
            result = convertView;
        }
        viewHolder.userFullname.setText(values.get(position).get("fullname"));
        viewHolder.username.setText(values.get(position).get("username"));
        if (values.get(position).get("selected").equals("1")) {
            Log.i("UIHSDCIUHVDIU", "" + position);
            viewHolder.search_layout.setBackgroundColor(Color.parseColor("#839496"));
            viewHolder.userFullname.setTextColor(Color.parseColor("#000000"));
            viewHolder.username.setTextColor(Color.parseColor("#000000"));
        } else {
            viewHolder.search_layout.setBackgroundColor(Color.parseColor("#00000000"));
            viewHolder.userFullname.setTextColor(Color.parseColor("#FFFFFF"));
            viewHolder.username.setTextColor(Color.parseColor("#FFFFFF"));
        }

        final int pos = position;
        Picasso.with(context)
                .load(values.get(position).get("image"))
                .networkPolicy(NetworkPolicy.OFFLINE)
                .transform(new CropCircleTransformation())
                .fit()
                .into(viewHolder.icon, new Callback() {
                    @Override
                    public void onSuccess() {
                    }

                    @Override
                    public void onError() {
                        // Try again online if cache failed
                        Picasso.with(context)
                                .load(values.get(pos).get("image"))
                                .transform(new CropCircleTransformation())
//                                .placeholder(R.drawable.default_profile)
//                                .error(R.drawable.ic_action_error)
                                .fit()
                                .into(viewHolder.icon);
                    }
                });

        return convertView;
    }

    //
    private static class ViewHolder {

        TextView userFullname;
        TextView username;
        ImageView icon;
        LinearLayout search_layout;
    }


}