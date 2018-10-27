package com.morlunk.mumbleclient.app;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Shader;
import android.os.AsyncTask;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.morlunk.mumbleclient.R;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
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
      viewHolder.icon = (ImageView) convertView.findViewById(R.id.appIconIV);

      result = convertView;
      convertView.setTag(viewHolder);
    } else {
      viewHolder = (ViewHolder) convertView.getTag();
      result = convertView;
    }
    viewHolder.userFullname.setText(values.get(position).get("fullname"));
    viewHolder.username.setText(values.get(position).get("username"));
    new AsyncTaskLoadImage(viewHolder.icon).execute(values.get(position).get("image"));
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
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inSampleSize = 8;
        bitmap = BitmapFactory.decodeStream((InputStream)url.getContent(), null, options);

      } catch (IOException e) {
        Log.e(TAG, e.getMessage());
      }
      return bitmap;
    }
    @Override
    protected void onPostExecute(Bitmap bitmap) {
      Bitmap circleBitmap = null;
      circleBitmap = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Bitmap.Config.ARGB_4444);
      BitmapShader shader = new BitmapShader (bitmap,  Shader.TileMode.CLAMP, Shader.TileMode.CLAMP);
      Paint paint = new Paint();
      paint.setShader(shader);
      paint.setAntiAlias(true);
      Canvas c = new Canvas(circleBitmap);
      c.drawCircle(bitmap.getWidth()/2, bitmap.getHeight()/2, bitmap.getWidth()/2, paint);
      imageView.setImageBitmap(circleBitmap);
    }
  }
}