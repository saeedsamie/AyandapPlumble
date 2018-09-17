package com.morlunk.mumbleclient.app;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;


public class RecentChatsListAdapter extends BaseAdapter {

  Context context;
  private final ArrayList<HashMap<String, String>> values;

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
  public View getView(int position, @Nullable View convertView, ViewGroup parent) {


    ViewHolder viewHolder;

    final View result;

    if (convertView == null) {

      viewHolder = new ViewHolder();
      LayoutInflater inflater = LayoutInflater.from(context);
//      convertView = inflater.inflate(R.layout.hotel_single_item, parent, false);
//      viewHolder.txtName = (TextView) convertView.findViewById(R.id.aNametxt);
//      viewHolder.txtId = (TextView) convertView.findViewById(R.id.aVersiontxt);
//      viewHolder.txtAddress = (TextView) convertView.findViewById(R.id.aAdrress);
//      viewHolder.icon = (ImageView) convertView.findViewById(R.id.appIconIV);

      result = convertView;

      convertView.setTag(viewHolder);
    } else {
      viewHolder = (ViewHolder) convertView.getTag();
      result = convertView;
    }
    viewHolder.txtName.setText(values.get(position).get("hotel_id"));
    viewHolder.txtId.setText(values.get(position).get("hotel_name"));
    viewHolder.txtAddress.setText(values.get(position).get("hotel_address"));
    new AsyncTaskLoadImage(viewHolder.icon).execute(values.get(position).get("hotel_image"));
    return convertView;
  }

  //
  private static class ViewHolder {

    TextView txtName;
    TextView txtId;
    TextView txtAddress;
    ImageView icon;
  }
  public class AsyncTaskLoadImage  extends AsyncTask<String, String, Bitmap> {
    private final static String TAG = "AsyncTaskLoadImage";
    private ImageView imageView;
    public AsyncTaskLoadImage(ImageView imageView) {
      this.imageView = imageView;
    }
    @Override
    protected Bitmap doInBackground(String... params) {
      Bitmap bitmap = null;
      try {
        URL url = new URL(params[0]);
        bitmap = BitmapFactory.decodeStream((InputStream)url.getContent());
      } catch (IOException e) {
        Log.e(TAG, e.getMessage());
      }
      return bitmap;
    }
    @Override
    protected void onPostExecute(Bitmap bitmap) {
      imageView.setImageBitmap(bitmap);
    }
  }
}