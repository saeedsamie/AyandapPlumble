package com.morlunk.mumbleclient.app;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Chronometer;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.morlunk.jumble.IJumbleService;
import com.morlunk.mumbleclient.R;
import com.morlunk.mumbleclient.service.IPlumbleService;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import jp.wasabeef.picasso.transformations.CropCircleTransformation;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

import static com.morlunk.mumbleclient.R.drawable;
import static com.morlunk.mumbleclient.R.id;

public class ChatActivity extends AppCompatActivity {

    Chronometer cmTimer;
    ImageView image;
    TextView title;
    TextView bio;
    LinearLayout chat_layout;
    private ProgressDialog mConnectingDialog;
    private AlertDialog mErrorDialog;
    private IPlumbleService service;
    private boolean resume = false;
    private long elapsedTime;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            getWindow().getDecorView().setLayoutDirection(View.LAYOUT_DIRECTION_RTL);
        }
        setContentView(R.layout.activity_chat);
        final int chatId = getIntent().getIntExtra("chatId", -1);
        String chatTitle = getIntent().getStringExtra("fullname");
        Log.i("VDKHBVDKHBSDVKHBVSDKHB", chatTitle);
        cmTimer = (Chronometer) findViewById(R.id.cmTimer);
        chat_layout = findViewById(R.id.chat_layout);
        final String finalChatTitle = chatTitle;
        chat_layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ChatActivity.this, GroupInfoActivity.class);

                Log.i("INTENTYPUTS", getIntent().getStringExtra("type") + "///" + getIntent().getStringExtra("ChatTitle") + "///" + getIntent().getStringExtra("chatId") + "///" + getIntent().getStringExtra("bio"));

                intent.putExtra("type", getIntent().getStringExtra("type"));
                intent.putExtra("ChatTitle", finalChatTitle);
                intent.putExtra("chatId", chatId + "");
                intent.putExtra("bio", getIntent().getStringExtra("bio"));
                if (getIntent().getStringExtra("type").equals("group")) {
                    startActivity(intent);
                }
            }
        });
        image = findViewById(id.c_image);
        title = findViewById(id.c_name);
        bio = findViewById(id.c_bio);
        cmTimer.setOnChronometerTickListener(new Chronometer.OnChronometerTickListener() {
            public void onChronometerTick(Chronometer arg0) {
                if (!resume) {
                    long minutes = ((SystemClock.elapsedRealtime() - cmTimer.getBase()) / 1000) / 60;
                    long seconds = ((SystemClock.elapsedRealtime() - cmTimer.getBase()) / 1000) % 60;
                    elapsedTime = SystemClock.elapsedRealtime();

                    Log.d("choronometer", "onChronometerTick: " + minutes + " : " + seconds);
                }
            }
        });

        if ((chatId != -1 && chatTitle != null)) {
//            textView.setText(chatId + "\n" + chatTitle);
        }
        chatTitle = "ChatTitle";
        this.setTitle(chatTitle);
        if (getIntent().getStringExtra("type").equals("pv")) {
            bio.setVisibility(View.GONE);
            title.setText(getIntent().getStringExtra("fullname"));

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
                            Picasso.with(ChatActivity.this)
                                    .load(getIntent().getStringExtra("image"))
                                    .transform(new CropCircleTransformation())
//                                .placeholder(R.drawable.default_profile)
//                                .error(R.drawable.ic_action_error)
                                    .fit()
                                    .into(image);
                        }
                    });
        } else {
            title.setText(getIntent().getStringExtra("fullname"));
            bio.setText(getIntent().getStringExtra("bio"));
        }
//        this.setServiceBound(true);
        final ImageView pushButton = (ImageView) findViewById(id.push_button);
        final int width = 400;
        final int hight = 400;
        pushButton.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        pushButton.setBackgroundResource(drawable.push_to_talk_pressed);
                        PlumbleActivity.mService.onTalkKeyDown();
                        break;
                    case MotionEvent.ACTION_UP:
                        pushButton.setBackgroundResource(drawable.push_to_talk_unpressed);
                        PlumbleActivity.mService.onTalkKeyUp();
                        break;
                }
                return true;
            }
        });

    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

    @Override
    public void onBackPressed() {

        IJumbleService iPlumbleService = PlumbleActivity.mService;
        int parentChannel;
        try {
            parentChannel = iPlumbleService.getSession().getSessionChannel().getParent().getId();
        } catch (Exception e) {
            parentChannel = 0;
            e.printStackTrace();
        }
        try {
            iPlumbleService.getSession().joinChannel(iPlumbleService.getSession().getRootChannel().getId());
        } catch (Exception e) {
            e.printStackTrace();
        }
        super.onBackPressed();
    }

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {

    }
}
