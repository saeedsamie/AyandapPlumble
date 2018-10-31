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

import com.jakewharton.picasso.OkHttp3Downloader;
import com.morlunk.mumbleclient.R;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;

import jp.wasabeef.picasso.transformations.CropCircleTransformation;
import okhttp3.Cache;
import okhttp3.OkHttpClient;


public class SearchListAdapter extends BaseAdapter {
  public static Picasso picassoWithCache;
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

      result = convertView;
      convertView.setTag(viewHolder);
    } else {
      viewHolder = (ViewHolder) convertView.getTag();
      result = convertView;
    }
    viewHolder.userFullname.setText(values.get(position).get("fullname"));
    viewHolder.username.setText(values.get(position).get("username"));
//    new AsyncTaskLoadImage(viewHolder.icon).execute(values.get(position).get("image"));
//     Glide.with(context)
//      .load(values.get(position).get("image"))
//       .apply(RequestOptions.circleCropTransform())
//      .into(viewHolder.icon);

//    Picasso.with(context)
//      .load(values.get(position).get("image"))
//      .networkPolicy(NetworkPolicy.OFFLINE)
//      .into(viewHolder.icon, new Callback() {
//        @Override
//        public void onSuccess() {
//
//        }
//
//        @Override
//        public void onError() {
//          //Try again online if cache failed
//          Picasso.with(context)
//            .load(LoginActivity.URL+"profile_image/0.png")
//            .error(R.drawable.ic_action_error)
//            .into(viewHolder.icon, new Callback() {
//              @Override
//              public void onSuccess() {
//
//              }
//
//              @Override
//              public void onError() {
//                Log.v("Picasso","Could not fetch image");
//              }
//            });
//        }
//      });

    File httpCacheDirectory = new File(context.getCacheDir(), "picasso-cache");
    Cache cache = new Cache(httpCacheDirectory, 15 * 1024 * 1024);
    OkHttpClient.Builder okHttpClientBuilder = new OkHttpClient.Builder().cache(cache);
    picassoWithCache = new Picasso.Builder(context).downloader(new OkHttp3Downloader(okHttpClientBuilder.build())).build();
    picassoWithCache.load(values.get(position).get("image")).transform(new CropCircleTransformation()).into(viewHolder.icon);



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

      } catch (Exception e) {
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