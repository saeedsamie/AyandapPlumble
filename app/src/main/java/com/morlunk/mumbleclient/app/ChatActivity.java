package com.morlunk.mumbleclient.app;

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
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.jakewharton.picasso.OkHttp3Downloader;
import com.morlunk.jumble.IJumbleService;
import com.morlunk.jumble.IJumbleSession;
import com.morlunk.jumble.util.JumbleDisconnectedException;
import com.morlunk.mumbleclient.R;
import com.morlunk.mumbleclient.service.IPlumbleService;
import com.squareup.picasso.Picasso;

import java.io.File;

import jp.wasabeef.picasso.transformations.CropCircleTransformation;
import okhttp3.Cache;
import okhttp3.OkHttpClient;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

import static com.morlunk.mumbleclient.R.drawable;
import static com.morlunk.mumbleclient.R.id;

public class ChatActivity extends AppCompatActivity {

    PlumbleActivity plumbleActivity;
    Chronometer cmTimer;
    private ProgressDialog mConnectingDialog;
    private AlertDialog mErrorDialog;
    private IPlumbleService service;
    private boolean resume = false;
    private long elapsedTime;
  public static Picasso picassoWithCache;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            getWindow().getDecorView().setLayoutDirection(View.LAYOUT_DIRECTION_RTL);
        }

        setContentView(R.layout.activity_chat);
        TextView textView = (TextView) findViewById(id.chat_title);
        int chatId = getIntent().getIntExtra("chatId", -1);
        String chatTitle = getIntent().getStringExtra("ChatTitle");
        cmTimer = (Chronometer) findViewById(R.id.cmTimer);

        cmTimer.setOnChronometerTickListener(new Chronometer.OnChronometerTickListener() {
            public void onChronometerTick(Chronometer arg0) {
                if (!resume) {
                    long minutes = ((SystemClock.elapsedRealtime() - cmTimer.getBase()) / 1000) / 60;
                    long seconds = ((SystemClock.elapsedRealtime() - cmTimer.getBase()) / 1000) % 60;
                    elapsedTime = SystemClock.elapsedRealtime();

                    Log.d("choronometer", "onChronometerTick: " + minutes + " : " + seconds);
                }
//                else {
//                    long minutes = ((elapsedTime - cmTimer.getBase()) / 1000) / 60;
//                    long seconds = ((elapsedTime - cmTimer.getBase()) / 1000) % 60;
//                    elapsedTime = elapsedTime + 1000;
//                    Log.d("choronometer", "onChronometerTick: " + minutes + " : " + seconds);
//                }
            }
        });

        if ((chatId != -1 && chatTitle != null)) {
//            textView.setText(chatId + "\n" + chatTitle);
    }
    chatTitle = "ChatTitle";
    this.setTitle(chatTitle);
    LinearLayout linearLayout = (LinearLayout) findViewById(id.chat_header);
    linearLayout.setOrientation(LinearLayout.HORIZONTAL);

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
      this.setTitle(getIntent().getStringExtra("fullname"));
      ImageView ImageView = new ImageView(this);

      File httpCacheDirectory = new File(getBaseContext().getCacheDir(), "picasso-cache");
      Cache cache = new Cache(httpCacheDirectory, 15 * 1024 * 1024);
      OkHttpClient.Builder okHttpClientBuilder = new OkHttpClient.Builder().cache(cache);
      picassoWithCache = new Picasso.Builder(getBaseContext()).downloader(new OkHttp3Downloader(okHttpClientBuilder.build())).build();
      picassoWithCache.load(getIntent().getStringExtra("image")).transform(new CropCircleTransformation()).into(ImageView);

      ImageView.setBackgroundResource(drawable.round_button);
      ImageView.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View v) {
        }
      });
      LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(250, 250);
      layoutParams.setMargins(5, 5, 5, 5);
      ImageView.setPadding(3, 3, 3, 3);
      linearLayout.addView(ImageView, layoutParams);
    } else {
      for (int i = 0; i < 10; i++) {
        ImageButton imageButton = new ImageButton(this);
        imageButton.setImageResource(drawable.ic_launcher);
        imageButton.setBackgroundResource(drawable.round_button);
        final int finalI = i;
        imageButton.setOnClickListener(new View.OnClickListener() {
          @Override
          public void onClick(View v) {
            Log.d("Image View ", String.valueOf(finalI));
          }
        });
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(250, 250);
        layoutParams.setMargins(5, 5, 5, 5);
        imageButton.setPadding(3, 3, 3, 3);
        linearLayout.addView(imageButton, layoutParams);
      }
    }
//        this.setServiceBound(true);
        final ImageButton pushButton = (ImageButton) findViewById(id.push_button);
        final int width = 400;
        final int hight = 400;
        pushButton.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        pushButton.setBackgroundResource(drawable.push_to_talk_pressed);
                        PlumbleActivity.mService.onTalkKeyDown();
                        cmTimer.start();
                        cmTimer.setBase(SystemClock.elapsedRealtime());
                        resume = true;
                        break;
                    case MotionEvent.ACTION_UP:
                        pushButton.setBackgroundResource(drawable.push_to_talk_unpressed);
                        PlumbleActivity.mService.onTalkKeyUp();
                        cmTimer.stop();
                        cmTimer.setText("00:00");
                        cmTimer.setBase(SystemClock.elapsedRealtime());
                        resume = false;
                        break;
                }
                return true;
            }
        });

        final TextView timerView = findViewById(R.id.timer);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
//                timerView.setText(((PlumbleService) PlumbleActivity.mService).getTimer());
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
}
