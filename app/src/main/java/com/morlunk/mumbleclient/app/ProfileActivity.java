package com.morlunk.mumbleclient.app;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Shader;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.morlunk.mumbleclient.FilePath;
import com.morlunk.mumbleclient.OnTaskCompletedListener;
import com.morlunk.mumbleclient.R;
import com.morlunk.mumbleclient.ServerFetchAsync;
import com.morlunk.mumbleclient.util.AsyncLoadCircularImage;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

//import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;


public class ProfileActivity extends AppCompatActivity {

  private static final int PICK_FILE_REQUEST = 1;
  String userId ;
  SharedPreferences sp;
  AlertDialog.Builder alertBuilder;
  ImageView ivAttachment;
  Button bUpload;
  private String selectedFilePath;
  private EditText ed_fullname;
  private EditText ed_username;
  private ProgressDialog pDialog;
  private String SERVER_URL = LoginActivity.URL+"image.php";
  private TextView textView;
  private BackgroundTask backgroundTask;
  List<NameValuePair> nameValuePairs;
  static ProfileActivity profileActivity;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.profile);
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
      getWindow().getDecorView().setLayoutDirection(View.LAYOUT_DIRECTION_RTL);
    }
    getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);

    ActivityCompat.requestPermissions(ProfileActivity.this,
      new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1);

    userId = getSharedPreferences("MyPref", MODE_PRIVATE).getString(getString(R.string.PREF_TAG_userid), "-2");
    ed_fullname = (EditText) findViewById(R.id.profile_fullname);
    ed_username = (EditText) findViewById(R.id.profile_username);
    ed_username.setEnabled(false);
    ivAttachment = (ImageView) findViewById(R.id.profile_image);

    profileView();

    ivAttachment.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        showFileChooser();
      }
    });
    profileActivity = this;
    bUpload = (Button) findViewById(R.id.profile_save);
    bUpload.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {

        if (ed_fullname.getText().toString().isEmpty()) {
          ed_fullname.setError("نام خود را وارد کنید!");
        }
        else {
        profileUpdate();
        }
      }
    });

    sp = getSharedPreferences("MyPref", Context.MODE_PRIVATE);
    pDialog = new ProgressDialog(ProfileActivity.this);
    pDialog.setMessage("لطفا صبر کنید...");
    pDialog.setCancelable(false);
    alertBuilder = new AlertDialog.Builder(ProfileActivity.this);
  }

  private void showFileChooser() {
    Intent intent = new Intent();
    intent.setType("*/*");
    intent.setAction(Intent.ACTION_GET_CONTENT);
    startActivityForResult(Intent.createChooser(intent, "عکس پروفایل خود انتخاب کنید..."), PICK_FILE_REQUEST);
  }

  @Override
  protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    super.onActivityResult(requestCode, resultCode, data);
    if (resultCode == Activity.RESULT_OK) {
      if (requestCode == PICK_FILE_REQUEST) {
        if (data == null) {
          //no data present
          return;
        }

        Uri selectedFileUri = data.getData();
        selectedFilePath = FilePath.getPath(this, selectedFileUri);

        if (selectedFilePath != null && !selectedFilePath.equals("")) {

          String TAG = "AsyncTaskLoadImage";
          Bitmap bitmap = null;
          Bitmap circleBitmap = null;

          try {
            bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), selectedFileUri);
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
          ivAttachment.setImageBitmap(circleBitmap);

        } else {
          Toast.makeText(this, "Cannot upload file to server", Toast.LENGTH_SHORT).show();
        }
      }
    }
  }


  public int uploadFile(final String selectedFilePath) {

    int serverResponseCode = 0;

    HttpURLConnection connection;
    DataOutputStream dataOutputStream;
    String lineEnd = "\r\n";
    String twoHyphens = "--";
    String boundary = "*****";


    int bytesRead, bytesAvailable, bufferSize;
    byte[] buffer;
    int maxBufferSize = 1 * 1024 * 1024;
    File selectedFile = new File(selectedFilePath);


    String[] parts = selectedFilePath.split("/");
    final String fileName = parts[parts.length - 1];

    if (!selectedFile.isFile()) {

      runOnUiThread(new Runnable() {
        @Override
        public void run() {
          Log.i("SPOIMVSDPIM", "Source File Doesn't Exist");
        }
      });
      return 0;
    } else {
      try {
        FileInputStream fileInputStream = new FileInputStream(selectedFile);
        URL url = new URL(SERVER_URL);
        connection = (HttpURLConnection) url.openConnection();
        connection.setDoInput(true);//Allow Inputs
        connection.setDoOutput(true);//Allow Outputs
        connection.setUseCaches(false);//Don't use a cached Copy
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Connection", "Keep-Alive");
        connection.setRequestProperty("ENCTYPE", "multipart/form-data");
        connection.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + boundary);
        connection.setRequestProperty("uploaded_file", selectedFilePath);

        //creating new dataoutputstream
        dataOutputStream = new DataOutputStream(connection.getOutputStream());

        String[] tmp = selectedFilePath.split("/");
        tmp[tmp.length - 1] = userId + ".png";
        StringBuffer result = new StringBuffer();
        for (int i = 0; i < tmp.length; i++) {
          if (i < tmp.length - 1) {
            result.append(tmp[i] + '/');
          } else {
            result.append(tmp[i]);
          }
        }
        String mynewstring = result.toString();
        dataOutputStream.writeBytes(twoHyphens + boundary + lineEnd);
        dataOutputStream.writeBytes("Content-Disposition: form-data; name=\"uploaded_file\";filename=\""
          + mynewstring + "\"" + lineEnd);
        dataOutputStream.writeBytes(lineEnd);
        bytesAvailable = fileInputStream.available();
        bufferSize = Math.min(bytesAvailable, maxBufferSize);
        buffer = new byte[bufferSize];
        bytesRead = fileInputStream.read(buffer, 0, bufferSize);
        while (bytesRead > 0) {
          dataOutputStream.write(buffer, 0, bufferSize);
          bytesAvailable = fileInputStream.available();
          bufferSize = Math.min(bytesAvailable, maxBufferSize);
          bytesRead = fileInputStream.read(buffer, 0, bufferSize);
        }

        dataOutputStream.writeBytes(lineEnd);
        dataOutputStream.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd);

        serverResponseCode = connection.getResponseCode();
        String serverResponseMessage = connection.getResponseMessage();
        if (serverResponseCode == 200) {
          runOnUiThread(new Runnable() {
            @Override
            public void run() {
              Log.i("SPOIMVSDPIM", "UPLOADED SUCCESSFULLY");
              backgroundTask = new BackgroundTask(textView);
              backgroundTask.execute();
            }
          });
        }
        fileInputStream.close();
        dataOutputStream.flush();
        dataOutputStream.close();
      } catch (FileNotFoundException e) {
        e.printStackTrace();
        runOnUiThread(new Runnable() {
          @Override
          public void run() {
            Toast.makeText(ProfileActivity.this, "File Not Found", Toast.LENGTH_SHORT).show();
          }
        });
      } catch (MalformedURLException e) {
        e.printStackTrace();
        Toast.makeText(ProfileActivity.this, "URL error!", Toast.LENGTH_SHORT).show();

      } catch (IOException e) {
        e.printStackTrace();
        Toast.makeText(ProfileActivity.this, "Cannot Read/Write File!", Toast.LENGTH_SHORT).show();
      } catch (Exception e) {
        e.printStackTrace();
      }
      return serverResponseCode;
    }

  }


  @Override
  public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
    switch (requestCode) {
      case 1: {
        // If request is cancelled, the result arrays are empty.
        if (grantResults.length > 0
          && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
          // permission granted and now can proceed

        } else {

          // permission denied, boo! Disable the
          // functionality that depends on this permission.
          Toast.makeText(ProfileActivity.this, "Permission denied to read your External storage", Toast.LENGTH_SHORT).show();
        }
        return;
      }
      // add other cases for more permissions
    }
  }

  @Override
  protected void onDestroy() {
    super.onDestroy();
    if (backgroundTask != null) {
      backgroundTask.cancel(true);
    }
  }

  @Override
  protected void attachBaseContext(Context newBase) {
    super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
  }



  private class BackgroundTask extends AsyncTask<Void, Void, String> {

    private final WeakReference<TextView> messageViewReference;

    private BackgroundTask(TextView textView) {
      this.messageViewReference = new WeakReference<>(textView);
    }


    @Override
    protected String doInBackground(Void... voids) {

      HttpClient httpclient = new DefaultHttpClient();
      HttpPost httppost = new HttpPost(LoginActivity.URL+"sqlite.php");
      try {
        List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
        nameValuePairs.add(new BasicNameValuePair("func", "imageInsert"));
        nameValuePairs.add(new BasicNameValuePair("userId", userId));
        nameValuePairs.add(new BasicNameValuePair("imageURL", userId + ".png"));
        Log.e("mainToPost", "mainToPost" + nameValuePairs.toString());
        UrlEncodedFormEntity form;
        form = new UrlEncodedFormEntity(nameValuePairs, "UTF-8");
        // Use UrlEncodedFormEntity to send in proper format which we need
        httppost.setEntity(form);
        HttpResponse response = httpclient.execute(httppost);
        InputStream inputStream = response.getEntity().getContent();
        InputStreamToStringExample str = new InputStreamToStringExample();
      } catch (Exception e) {
        e.printStackTrace();
      }
      return null;
    }

    @Override
    protected void onPostExecute(String s) {
      super.onPostExecute(s);
      TextView textView = messageViewReference.get();
      if (textView != null) {
        textView.setText(s);
      }
    }


    public class InputStreamToStringExample {

      public void main(String[] args) throws IOException {

        // intilize an InputStream
        InputStream is = new ByteArrayInputStream("file content..blah blah".getBytes());

        String result = getStringFromInputStream(is);

        System.out.println(result);
        System.out.println("Done");

      }

      // convert InputStream to String
      public String getStringFromInputStream(InputStream is) {

        BufferedReader br = null;
        StringBuilder sb = new StringBuilder();

        String line;
        try {

          br = new BufferedReader(new InputStreamReader(is));
          while ((line = br.readLine()) != null) {
            sb.append(line);
          }

        } catch (IOException e) {
          e.printStackTrace();
        } finally {
          if (br != null) {
            try {
              br.close();
            } catch (IOException e) {
              e.printStackTrace();
            }
          }
        }
        return sb.toString();
      }

    }
  }


  public void profileView(){
    nameValuePairs = new ArrayList<NameValuePair>();
    nameValuePairs.add(new BasicNameValuePair("func", "profileView"));
    nameValuePairs.add(new BasicNameValuePair("userId", userId));
    new ServerFetchAsync(nameValuePairs, new OnTaskCompletedListener() {
      @Override
      public void onTaskCompleted(JSONObject jsonObject) {
          final ArrayList<HashMap<String, String>> listValues = new ArrayList<>();
          try {
            JSONArray jsonArray;
            jsonArray = jsonObject.getJSONArray("profile");

            for (int i = 0; i < jsonArray.length(); i++) {
              JSONObject c = jsonArray.getJSONObject(i);

              ed_fullname.setText(c.getString("fullname"));
              ed_username.setText(c.getString("username"));
              new AsyncLoadCircularImage(ivAttachment).execute(LoginActivity.URL+"profile_image/" + c.getString("image"));
            }

          } catch (Exception e) {
            e.printStackTrace();
          }
      }
    }).execute();
  }

  public void profileUpdate(){
    nameValuePairs = new ArrayList<NameValuePair>();
    nameValuePairs.add(new BasicNameValuePair("func", "profileUpdate"));
    nameValuePairs.add(new BasicNameValuePair("userId", userId));
    nameValuePairs.add(new BasicNameValuePair("fullname", ed_fullname.getText().toString()));

    new ServerFetchAsync(nameValuePairs, new OnTaskCompletedListener() {
      @Override
      public void onTaskCompleted(JSONObject jsonObject) {
        try {
            if (jsonObject.getString("profile").equals("UPDATED"))
            {
              finish();
            }
            else
            {
              Snackbar
                .make(findViewById(android.R.id.content),"خطایی پیش آمده ، مجددا تلاش کنید", Snackbar.LENGTH_SHORT)
                .show();
            }

          } catch (JSONException e1) {
          e1.printStackTrace();
        }
      }
    }).execute();
  }
}
