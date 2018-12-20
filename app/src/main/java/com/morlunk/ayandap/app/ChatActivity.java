package com.morlunk.ayandap.app;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.jakewharton.picasso.OkHttp3Downloader;
import com.morlunk.ayandap.R;
import com.morlunk.ayandap.calligraphy.CalligraphyContextWrapper;
import com.morlunk.ayandap.service.IPlumbleService;
import com.morlunk.ayandap.util.JumbleServiceFragment;
import com.morlunk.ayandap.util.JumbleServiceProvider;
import com.morlunk.jumble.IJumbleService;
import com.morlunk.jumble.IJumbleSession;
import com.morlunk.jumble.audio.AudioOutput;
import com.morlunk.jumble.model.IUser;
import com.squareup.picasso.Picasso;

import java.io.File;

import jp.wasabeef.picasso.transformations.CropCircleTransformation;
import okhttp3.Cache;
import okhttp3.OkHttpClient;

import static com.morlunk.ayandap.R.drawable;
import static com.morlunk.ayandap.R.id;

public class ChatActivity extends AppCompatActivity implements JumbleServiceProvider {

    public static Picasso picassoWithCache;
    Chronometer cmTimer;
    ImageView image;
    TextView title;
    TextView bio;
    Button mute;
    private boolean resume = false;
    private LinearLayout group_info_layout;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            getWindow().getDecorView().setLayoutDirection(View.LAYOUT_DIRECTION_RTL);
        }
        setContentView(R.layout.activity_chat);
        int chatId = getIntent().getIntExtra("chatId", -1);
        String chatTitle = getIntent().getStringExtra("ChatTitle");
        cmTimer = findViewById(id.cmTimer);

        group_info_layout = findViewById(id.group_info_layout);
        group_info_layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ChatActivity.this, GroupInfoActivity.class);
                intent.putExtra("bio", getIntent().getStringExtra("bio"));
                intent.putExtra("chatId", getIntent().getStringExtra("chatId"));
                intent.putExtra("ChatTitle", getIntent().getStringExtra("fullname"));
                if (getIntent().getStringExtra("type").equals("group")) {
                    startActivity(intent);
                }

            }
        });
        mute = findViewById(id.mute_button);
        mute.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final IJumbleSession session = PlumbleActivity.mService.getSession();
                IUser self = session.getSessionUser();
                final boolean deafened = !self.isSelfDeafened();
                session.setSelfMuteDeafState(deafened, deafened);
                if (deafened)
                    mute.setText("UnMute");
                else
                    mute.setText("Mute");

                final Handler handler = new Handler();
                Runnable runnable = new Runnable() {
                    @Override
                    public void run() {
                        IUser self = session.getSessionUser();
                        session.setSelfMuteDeafState(deafened, deafened);
                        handler.postDelayed(this, 100);
                        if (deafened == false && !self.isSelfDeafened()) {
                            handler.removeCallbacksAndMessages(null);
                        }else if(deafened == true && self.isSelfDeafened()){
                            handler.removeCallbacksAndMessages(null);
                        }
                    }
                };
                handler.postDelayed(runnable, 10);
            }
        });

//          IUser user = getSessionUser();
//        if (isConnectionEstablished() && user != null) {
//            Toast.makeText(getBaseContext(), "TOOOOOGGGGGGLLLLEED", Toast.LENGTH_LONG).show();
//            Log.i("MMMMMMMMMUTE","TOGGGGGGLED");
//            boolean muted = !user.isSelfMuted();
//            boolean deafened = user.isSelfDeafened() && muted;
//            setSelfMuteDeafState(muted, deafened);
//        }


        image = findViewById(id.c_image);
        title = findViewById(id.c_name);
        bio = findViewById(id.c_bio);

        cmTimer.setOnChronometerTickListener(new Chronometer.OnChronometerTickListener() {
            public void onChronometerTick(Chronometer arg0) {
                long minutes, seconds;
                if (!resume) {
                    minutes = ((SystemClock.elapsedRealtime() - cmTimer.getBase()) / 1000) / 60;
                    seconds = ((SystemClock.elapsedRealtime() - cmTimer.getBase()) / 1000) % 60;

                    Log.d("choronometer", "onChronometerTick: " + minutes + " : " + seconds);
                    Log.d("ascv", ":        " + resume);
                }


            }
        });
        Boolean isPv = false;
        try {
            isPv = getIntent().getStringExtra("type").equals("pv");
        } catch (Exception ignored) {
        }
        if (isPv) {
            bio.setVisibility(View.GONE);
            title.setText(getIntent().getStringExtra("fullname"));
            File httpCacheDirectory = new File(getBaseContext().getCacheDir(), "picasso-cache");
            Cache cache = new Cache(httpCacheDirectory, 15 * 1024 * 1024);
            OkHttpClient.Builder okHttpClientBuilder = new OkHttpClient.Builder().cache(cache);
            picassoWithCache = new Picasso.Builder(getBaseContext()).downloader(new OkHttp3Downloader(okHttpClientBuilder.build())).build();
            picassoWithCache.load(getIntent().getStringExtra("image")).transform(new CropCircleTransformation()).into(image);

        } else {
            title.setText(getIntent().getStringExtra("fullname"));
            bio.setText(getIntent().getStringExtra("bio"));
        }
//        this.setServiceBound(true);
        final ImageView pushButton = findViewById(id.push_button);
        pushButton.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        //start and stop counter
                        cmTimer.setBase(SystemClock.elapsedRealtime());
                        cmTimer.start();
                        //
                        long time = System.currentTimeMillis();
                        AudioOutput.log += time + "\n";

                        pushButton.setBackgroundResource(drawable.ptt_pressed);
                        PlumbleActivity.mService.onTalkKeyDown();
                        break;


                    case MotionEvent.ACTION_UP:
                        //start and stop counter
                        cmTimer.setBase(SystemClock.elapsedRealtime());
                        cmTimer.stop();

                        pushButton.setBackgroundResource(drawable.ptt_unpressed);
                        PlumbleActivity.mService.onTalkKeyUp();
                        break;
                }
                return true;
            }
        });

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

    @Override
    public IPlumbleService getService() {
        return null;
    }

    @Override
    public void addServiceFragment(JumbleServiceFragment fragment) {

    }

    @Override
    public void removeServiceFragment(JumbleServiceFragment fragment) {

    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }
}
