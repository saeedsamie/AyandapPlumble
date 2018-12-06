package com.morlunk.mumbleclient.app;


import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.jakewharton.picasso.OkHttp3Downloader;
import com.morlunk.mumbleclient.FilePath;
import com.morlunk.mumbleclient.OnTaskCompletedListener;
import com.morlunk.mumbleclient.R;
import com.morlunk.mumbleclient.ServerFetchAsync;
import com.squareup.picasso.Picasso;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import jp.wasabeef.picasso.transformations.CropCircleTransformation;
import okhttp3.Cache;
import okhttp3.OkHttpClient;

public class GroupInfoActivity extends AppCompatActivity {

    private ListView listView;
    private TextView bio;
    private ImageView image;
    public static Picasso picassoWithCache;
    List<NameValuePair> nameValuePairs;
    FloatingActionButton floatingActionButton;
    private static final int PICK_FILE_REQUEST = 1;
    private String selectedFilePath;
    ArrayList<HashMap<String, String>> listValues;
    ChatUsersListAdapter adapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_info);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            getWindow().getDecorView().setLayoutDirection(View.LAYOUT_DIRECTION_RTL);
        }

        listView = findViewById(R.id.group_info_listview);
        bio = findViewById(R.id.group_info_bio);
        image = findViewById(R.id.group_info_image);
        floatingActionButton = findViewById(R.id.group_image_button);


        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showFileChooser();
            }
        });


        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, final View view, final int position, long id) {

                AlertDialog.Builder builderSingle = new AlertDialog.Builder(GroupInfoActivity.this);
                builderSingle.setTitle(listValues.get(position).get("name"));

                final ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(GroupInfoActivity.this, android.R.layout.simple_list_item_1);
                arrayAdapter.add("mute");
                arrayAdapter.add("deafen");
                arrayAdapter.add("mute & deafen");
                arrayAdapter.add("kick");
                arrayAdapter.add("make admin");

                builderSingle.setAdapter(arrayAdapter, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, final int which) {
//                        String strName = arrayAdapter.getItem(which);
                        switch (arrayAdapter.getItem(which))
                        {
                            case "make admin" :
                                final CharSequence[] items = {"اضافه کردن کاربر","حذف کردن کاربر","تغییر عکس","Mute کردن کاربر","Deafen کردن کاربر","اضافه کردن ادمین"};
                                final ArrayList selectedItems=new ArrayList();
                                AlertDialog.Builder builder = new AlertDialog.Builder(view.getContext());
                                builder.setTitle("دسترسی های ادمین");
                                builder.setMultiChoiceItems(items, null, new DialogInterface.OnMultiChoiceClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which, boolean isChecked) {
                                        if (isChecked) {
                                            selectedItems.add(which);
                                        } else if (selectedItems.contains(which)) {
                                            selectedItems.remove(Integer.valueOf(which));
                                        }
                                    }
                                });
                                builder.setPositiveButton("اعمال", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                    }
                                });
                                builder.setNegativeButton("انصراف", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.dismiss();
                                        //Do something else if you want
                                    }
                                });
                                builder.create();
                                builder.show();

                                break;

                            case "kick" :
                                nameValuePairs = new ArrayList<NameValuePair>();
                                nameValuePairs.add(new BasicNameValuePair("func", "kickUser"));
                                nameValuePairs.add(new BasicNameValuePair("userId", listValues.get(position).get("id")));
                                nameValuePairs.add(new BasicNameValuePair("chatId", getIntent().getStringExtra("chatId")));

                                new ServerFetchAsync(nameValuePairs, new OnTaskCompletedListener() {
                                    @Override
                                    public void onTaskCompleted(JSONObject jsonObject) {
                                        try {
                                            if (jsonObject.getString("USER").equals("KICKED"))
                                            {
                                                Snackbar.make(findViewById(android.R.id.content), "کاربر مورد نظر حذف شد", Snackbar.LENGTH_SHORT)
                                                  .show();
                                                listValues.remove(position);
                                                adapter.notifyDataSetChanged();
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
                                break;
                        }



                    }
                });
                builderSingle.show();



            }
        });


        Toolbar toolbar = (Toolbar)findViewById(R.id.group_info_toolbar);
        setSupportActionBar(toolbar);
        if(getSupportActionBar() != null)
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        String chatTitle = getIntent().getStringExtra("ChatTitle");
        this.setTitle("  "+chatTitle);
        bio.setText(getIntent().getStringExtra("bio"));
        File httpCacheDirectory = new File(this.getCacheDir(), "picasso-cache");
        Cache cache = new Cache(httpCacheDirectory, 15 * 1024 * 1024);
        OkHttpClient.Builder okHttpClientBuilder = new OkHttpClient.Builder().cache(cache);
        picassoWithCache = new Picasso.Builder(this).downloader(new OkHttp3Downloader(okHttpClientBuilder.build())).build();
        picassoWithCache.load(LoginActivity.URL+"profile_image/" + "def" + ".png").transform(new CropCircleTransformation()).fit().into(image);

        nameValuePairs = new ArrayList<NameValuePair>();
        nameValuePairs.add(new BasicNameValuePair("func", "getUsers"));
        nameValuePairs.add(new BasicNameValuePair("chatId", getIntent().getStringExtra("chatId")));
        Log.i("kighbsdhb",getIntent().getStringExtra("chatId"));
        new ServerFetchAsync(nameValuePairs, new OnTaskCompletedListener() {
            @Override
            public void onTaskCompleted(JSONObject jsonObject) {
                listValues = new ArrayList<>();
                try {
                    JSONArray jsonArray;
                    jsonArray = jsonObject.getJSONArray("getUsers");

                    Log.i("TESTJSONNNN",jsonObject.toString());
                    for (int i = 0; i < jsonArray.length(); i++) {
                        HashMap hashMap = new HashMap();
                        JSONObject c = jsonArray.getJSONObject(i);

                        hashMap.put("name", c.getString("fullname"));
                        hashMap.put("property", c.getString("property"));
                        hashMap.put("access", c.getString("access"));
                        hashMap.put("image", LoginActivity.URL + "profile_image/" + c.getString("image"));
                        hashMap.put("id", c.getString("id"));

                        listValues.add(hashMap);
                    }



                    adapter = new ChatUsersListAdapter(GroupInfoActivity.this, listValues);
                    listView.setAdapter(adapter);
                    setListViewHeightBasedOnChildren(listView);

                } catch (Exception e) {
                    e.printStackTrace();
                }


            }}).execute();

    }

    public static void setListViewHeightBasedOnChildren(ListView listView) {
        ListAdapter listAdapter = listView.getAdapter();
        if (listAdapter == null)
            return;

        int desiredWidth = View.MeasureSpec.makeMeasureSpec(listView.getWidth(), View.MeasureSpec.UNSPECIFIED);
        int totalHeight = 0;
        View view = null;
        for (int i = 0; i < listAdapter.getCount(); i++) {
            view = listAdapter.getView(i, view, listView);
            if (i == 0)
                view.setLayoutParams(new ViewGroup.LayoutParams(desiredWidth, ViewGroup.LayoutParams.WRAP_CONTENT));

            view.measure(desiredWidth, View.MeasureSpec.UNSPECIFIED);
            totalHeight += view.getMeasuredHeight();
            totalHeight += 40;
            Log.i("HBDJHBVD",totalHeight+"");
        }
        ViewGroup.LayoutParams params = listView.getLayoutParams();
        params.height = totalHeight + (listView.getDividerHeight() * (listAdapter.getCount() - 1));
        listView.setLayoutParams(params);
    }

    private void showFileChooser() {
        Intent intent = new Intent();
        intent.setType("*/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "عکس گروه را انتخاب کنید..."), PICK_FILE_REQUEST);
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

                    } catch (IOException e) {
                        Log.e(TAG, e.getMessage());
                    }
                    image.setImageBitmap(bitmap);

                } else {
                    Toast.makeText(this, "Cannot upload file to server", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

}