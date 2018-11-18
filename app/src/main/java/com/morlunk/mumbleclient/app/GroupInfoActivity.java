package com.morlunk.mumbleclient.app;


import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.morlunk.mumbleclient.FilePath;
import com.morlunk.mumbleclient.OnTaskCompletedListener;
import com.morlunk.mumbleclient.R;
import com.morlunk.mumbleclient.ServerFetchAsync;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import jp.wasabeef.picasso.transformations.CropCircleTransformation;

public class GroupInfoActivity extends AppCompatActivity {

    private static final int PICK_FILE_REQUEST = 1;

    List<NameValuePair> nameValuePairs;
    FloatingActionButton floatingActionButton;
    private ListView listView;
    private TextView bio;
    private ImageView image;
    private String selectedFilePath;

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
            Log.i("HBDJHBVD", totalHeight + "");
        }
        ViewGroup.LayoutParams params = listView.getLayoutParams();
        params.height = totalHeight + (listView.getDividerHeight() * (listAdapter.getCount() - 1));
        listView.setLayoutParams(params);
    }

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


        Toolbar toolbar = (Toolbar) findViewById(R.id.group_info_toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null)
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        String chatTitle = getIntent().getStringExtra("ChatTitle");
        this.setTitle("  " + chatTitle);
        bio.setText(getIntent().getStringExtra("bio"));

        Picasso.with(this)
                .load(getIntent().getStringExtra("image"))
                .networkPolicy(NetworkPolicy.OFFLINE)
                .transform(new CropCircleTransformation())
                .fit()
                .into(image, new Callback() {
                    @Override
                    public void onSuccess() {
                    }

                    @Override
                    public void onError() {
                        // Try again online if cache failed
                        Picasso.with(GroupInfoActivity.this)
                                .load(getIntent().getStringExtra("image"))
                                .transform(new CropCircleTransformation())
//                                .placeholder(R.drawable.default_profile)
//                                .error(R.drawable.ic_action_error)
                                .fit()
                                .into(image);
                    }
                });
        nameValuePairs = new ArrayList<NameValuePair>();
        nameValuePairs.add(new BasicNameValuePair("func", "getUsers"));
        nameValuePairs.add(new BasicNameValuePair("chatId", getIntent().getStringExtra("chatId")));
        new ServerFetchAsync(nameValuePairs, new OnTaskCompletedListener() {
            @Override
            public void onTaskCompleted(JSONObject jsonObject) {
                final ArrayList<HashMap<String, String>> listValues = new ArrayList<>();
                try {
                    JSONArray jsonArray;
                    jsonArray = jsonObject.getJSONArray("getUsers");

                    for (int i = 0; i < jsonArray.length(); i++) {
                        HashMap hashMap = new HashMap();
                        JSONObject c = jsonArray.getJSONObject(i);

                        hashMap.put("name", c.getString("fullname"));
                        hashMap.put("role", c.getString("role"));
                        hashMap.put("image", LoginActivity.URL + "profile_image/" + c.getString("image"));
                        hashMap.put("id", c.getString("id"));

                        listValues.add(hashMap);
                    }

                    ChatUsersListAdapter adapter = new ChatUsersListAdapter(GroupInfoActivity.this, listValues);
                    listView.setAdapter(adapter);
                    setListViewHeightBasedOnChildren(listView);

                } catch (Exception e) {
                    e.printStackTrace();
                }


            }
        }).execute();

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