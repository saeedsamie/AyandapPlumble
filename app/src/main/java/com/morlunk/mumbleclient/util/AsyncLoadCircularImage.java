package com.morlunk.mumbleclient.util;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Shader;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.ImageView;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

public class AsyncLoadCircularImage  extends AsyncTask<String, String, Bitmap> {
  private final static String TAG = "AsyncTaskLoadImage";
  private ImageView imageView;
  public AsyncLoadCircularImage(ImageView imageView) {
    this.imageView = imageView;
  }
  @Override
  protected Bitmap doInBackground(String... params) {
    Bitmap bitmap = null;
    Bitmap circleBitmap = null;

    try {
      URL url = new URL(params[0]);
      bitmap = BitmapFactory.decodeStream((InputStream)url.getContent());

      circleBitmap = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Bitmap.Config.ARGB_4444);
      BitmapShader shader = new BitmapShader (bitmap,  Shader.TileMode.CLAMP, Shader.TileMode.CLAMP);
      Paint paint = new Paint();
      paint.setShader(shader);
      paint.setAntiAlias(true);
      Canvas c = new Canvas(circleBitmap);
      c.drawCircle(bitmap.getWidth()/2, bitmap.getHeight()/2, bitmap.getWidth()/2, paint);


    } catch (IOException e) {
      Log.e(TAG, e.getMessage());
    }
    return circleBitmap;
  }
  @Override
  protected void onPostExecute(Bitmap circleBitmap) {
    imageView.setImageBitmap(circleBitmap);
  }
}