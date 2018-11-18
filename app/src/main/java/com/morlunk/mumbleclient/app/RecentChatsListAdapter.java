package com.morlunk.mumbleclient.app;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Shader;
import android.os.AsyncTask;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
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


public class RecentChatsListAdapter extends BaseAdapter {

    private final ArrayList<HashMap<String, String>> values;
    Context context;

    public RecentChatsListAdapter(Context context, ArrayList<HashMap<String, String>> values) {
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
    public View getView(final int position, View convertView, ViewGroup parent) {


        final ViewHolder viewHolder;

        final View result;

        if (convertView == null) {

            viewHolder = new ViewHolder();
            LayoutInflater inflater = LayoutInflater.from(context);
            convertView = inflater.inflate(R.layout.chat_row, parent, false);
            viewHolder.chatTitle = convertView.findViewById(R.id.chatTitle);
            viewHolder.icon = convertView.findViewById(R.id.appIconIV);

            result = convertView;
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
            result = convertView;
        }

        viewHolder.chatTitle.setText(values.get(position).get("title"));
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
                                .load(values.get(position).get("image"))
                                .transform(new CropCircleTransformation())
//                                .placeholder(R.drawable.default_profile)
//                                .error(R.drawable.ic_action_error)
                                .fit()
                                .into(viewHolder.icon);
                    }
                });

        return convertView;
    }

    private static class ViewHolder {
        TextView chatTitle;
        ImageView icon;
    }

}
