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


    ViewHolder viewHolder;

    final View result;

    if (convertView == null) {

      viewHolder = new ViewHolder();
      LayoutInflater inflater = LayoutInflater.from(context);
      convertView = inflater.inflate(R.layout.search_row, parent, false);
      viewHolder.userFullname = (TextView) convertView.findViewById(R.id.userFullname);
      viewHolder.username = (TextView) convertView.findViewById(R.id.username);
//            viewHolder.icon = (ImageView) convertView.findViewById(R.id.appIconIV);

      result = convertView;
      convertView.setTag(viewHolder);
    } else {
      viewHolder = (ViewHolder) convertView.getTag();
      result = convertView;
    }
    viewHolder.userFullname.setText(values.get(position).get("fullname"));
    viewHolder.username.setText(values.get(position).get("username"));
    return convertView;
  }

  //
  private static class ViewHolder {

    TextView userFullname;
    TextView username;
    ImageView icon;
  }
//  public class AsyncTaskLoadImage  extends AsyncTask<String, String, Bitmap> {
//    private final static String TAG = "AsyncTaskLoadImage";
//    private ImageView imageView;
//    public AsyncTaskLoadImage(ImageView imageView) {
//      this.imageView = imageView;
//    }
//    @Override
//    protected Bitmap doInBackground(String... params) {
//      Bitmap bitmap = null;
//      try {
//        URL url = new URL(params[0]);
//        bitmap = BitmapFactory.decodeStream((InputStream)url.getContent());
//      } catch (IOException e) {
//        Log.e(TAG, e.getMessage());
//      }
//      return bitmap;
//    }
//    @Override
//    protected void onPostExecute(Bitmap bitmap) {
//      imageView.setImageBitmap(bitmap);
//    }
//  }
}