package com.morlunk.mumbleclient.app;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Chronometer;
import android.widget.ImageView;
import android.widget.TextView;

import com.jakewharton.picasso.OkHttp3Downloader;
import com.morlunk.jumble.IJumbleService;
import com.morlunk.jumble.IJumbleSession;
import com.morlunk.jumble.audio.AudioOutput;
import com.morlunk.jumble.util.JumbleDisconnectedException;
import com.morlunk.mumbleclient.R;
import com.morlunk.mumbleclient.service.IPlumbleService;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.util.Calendar;
import java.util.Date;

import jp.wasabeef.picasso.transformations.CropCircleTransformation;
import okhttp3.Cache;
import okhttp3.OkHttpClient;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

import static com.morlunk.mumbleclient.R.drawable;
import static com.morlunk.mumbleclient.R.id;

public class ChatActivity extends AppCompatActivity  {

    Chronometer cmTimer;
    private ProgressDialog mConnectingDialog;
    private AlertDialog mErrorDialog;
    private IPlumbleService service;
    private boolean resume = false;
    private long elapsedTime;
    ImageView image;
    TextView title;
    TextView bio ;
    public static Picasso picassoWithCache;


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
        cmTimer = (Chronometer) findViewById(R.id.cmTimer);

        image = findViewById(id.c_image);
        title = findViewById(id.c_name);
        bio = findViewById(id.c_bio);

        cmTimer.setOnChronometerTickListener(new Chronometer.OnChronometerTickListener() {
            public void onChronometerTick(Chronometer arg0) {
                long minutes , seconds;
                if (!resume) {
                    minutes = ((SystemClock.elapsedRealtime() - cmTimer.getBase()) / 1000) / 60;
                    seconds = ((SystemClock.elapsedRealtime() - cmTimer.getBase()) / 1000) % 60;
                    elapsedTime = SystemClock.elapsedRealtime();

                    Log.d("choronometer", "onChronometerTick: " + minutes + " : " + seconds);
                    Log.d("ascv", ":        " +resume);
                }


            }
        });

        if ((chatId != -1 && chatTitle != null)) {
//            textView.setText(chatId + "\n" + chatTitle);
        }
        chatTitle = "ChatTitle";
        this.setTitle(chatTitle);

        if (chatId!=-1)
            try {
                IJumbleSession session = PlumbleActivity.mService.getSession();
                session.joinChannel(chatId);
            } catch (JumbleDisconnectedException e) {
                e.printStackTrace();
            } catch (NumberFormatException e) {
                e.printStackTrace();
            }

        if (getIntent().getStringExtra("type").equals("pv")) {
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
        final ImageView pushButton = (ImageView) findViewById(id.push_button);
        final int width = 400;
        final int hight = 400;




        pushButton.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        //start and stop counter
                        cmTimer.setBase(SystemClock.elapsedRealtime());
                        cmTimer.start();
                        //

                        Date aa = Calendar.getInstance().getTime();
                        AudioOutput.log += aa.toString() + "\n";

                        //jgfjvjv

                        pushButton.setBackgroundResource(drawable.push_to_talk_pressed);
                        PlumbleActivity.mService.onTalkKeyDown();
                        break;


                    case MotionEvent.ACTION_UP:
                        //start and stop counter
                        cmTimer.setBase(SystemClock.elapsedRealtime());
                        cmTimer.stop();
                        //
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
