package com.morlunk.ayandap.app;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Chronometer;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.jakewharton.picasso.OkHttp3Downloader;
import com.morlunk.ayandap.R;
import com.morlunk.ayandap.service.IPlumbleService;
import com.morlunk.ayandap.util.JumbleServiceFragment;
import com.morlunk.ayandap.util.JumbleServiceProvider;
import com.morlunk.jumble.IJumbleSession;
import com.morlunk.jumble.audio.AudioOutput;
import com.morlunk.jumble.model.IUser;
import com.squareup.picasso.Picasso;

import org.jetbrains.annotations.Nullable;

import java.io.File;

import jp.wasabeef.picasso.transformations.CropCircleTransformation;
import okhttp3.Cache;
import okhttp3.OkHttpClient;


public class SendVoiceFragment extends Fragment implements JumbleServiceProvider {

  Chronometer cmTimer;
  private LinearLayout group_info_layout;
  private ImageView mute_image;
  private LinearLayout mute_linear;
  ImageView image;
  TextView title;
  TextView bio;
  private boolean resume = false;
  public static Picasso picassoWithCache;

  public SendVoiceFragment() {
    // Required empty public constructor
  }

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container,
                           Bundle savedInstanceState) {
    // Inflate the layout for this fragment
    return inflater.inflate(R.layout.fragment_send_voice, container, false);

  }

  @Override
  public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {


    cmTimer = getView().findViewById(R.id.cmTimer);
    mute_image = getView().findViewById(R.id.mute_image);
    mute_linear = getView().findViewById(R.id.mute_button);

    if (getActivity().getIntent().getStringExtra("type").equals("channel") && getActivity().getIntent().getStringExtra("access").equals("0"))
    {
      mute_linear.setVisibility(View.GONE);
    }

    Log.i("IHFIUH",getActivity().getIntent().getStringExtra("property"));

    if (getActivity().getIntent().getStringExtra("property").equals("0")) {
      setMuteDeafenFirstTime();
    }
    else
    {
      mute_image.setImageResource(R.drawable.muted);
      final int sdk = android.os.Build.VERSION.SDK_INT;
      if(sdk < android.os.Build.VERSION_CODES.JELLY_BEAN) {
        mute_linear.setBackgroundDrawable(ContextCompat.getDrawable(getContext(), R.drawable.rounded_speaker));
      } else {
        mute_linear.setBackground(ContextCompat.getDrawable(getContext(), R.drawable.rounded_speaker));
      }
//            mute_linear.setClickable(false);
      final IJumbleSession session = PlumbleActivity.mService.getSession();
      final boolean deafened = true;
      session.setSelfMuteDeafState(deafened, deafened);
      mute_linear.setEnabled(false);
    }

    group_info_layout = getView().findViewById(R.id.group_info_layout);
    group_info_layout.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        Intent intent = new Intent(getContext(), GroupInfoActivity.class);
        intent.putExtra("bio", getActivity().getIntent().getStringExtra("bio"));
        intent.putExtra("chatId", getActivity().getIntent().getStringExtra("chatId"));
        intent.putExtra("ChatTitle", getActivity().getIntent().getStringExtra("fullname"));
        intent.putExtra("property", getActivity().getIntent().getStringExtra("property"));
        intent.putExtra("access", getActivity().getIntent().getStringExtra("access"));
        if (getActivity().getIntent().getStringExtra("type").equals("group")) {
          startActivity(intent);
        }
      }
    });

    mute_linear.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        final IJumbleSession session = PlumbleActivity.mService.getSession();
        IUser self = session.getSessionUser();
        final boolean deafened = !self.isSelfDeafened();
        session.setSelfMuteDeafState(deafened, deafened);
        if (deafened) {
          mute_image.setImageResource(R.drawable.speakermuted);
          final int sdk = android.os.Build.VERSION.SDK_INT;
          if(sdk < android.os.Build.VERSION_CODES.JELLY_BEAN) {
            mute_linear.setBackgroundDrawable(ContextCompat.getDrawable(getContext(), R.drawable.rounded_muted_speaker) );
          } else {
            mute_linear.setBackground(ContextCompat.getDrawable(getContext(), R.drawable.rounded_muted_speaker));
          }
//                    Snackbar
//                      .make(findViewById(android.R.id.content), "حالت بيصدا فعال شد", Snackbar.LENGTH_SHORT)
//                      .show();
        }
        else {
          mute_image.setImageResource(R.drawable.speaker);
          final int sdk = android.os.Build.VERSION.SDK_INT;
          if(sdk < android.os.Build.VERSION_CODES.JELLY_BEAN) {
            mute_linear.setBackgroundDrawable(ContextCompat.getDrawable(getContext(), R.drawable.rounded_speaker) );
          } else {
            mute_linear.setBackground(ContextCompat.getDrawable(getContext(), R.drawable.rounded_speaker));
          }
//                    Snackbar
//                      .make(findViewById(android.R.id.content), "حالت بيصدا غيرفعال شد", Snackbar.LENGTH_SHORT)
//                      .show();
        }

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

    image = getView().findViewById(R.id.c_image);
    title = getView().findViewById(R.id.c_name);
    bio = getView().findViewById(R.id.c_bio);

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
      isPv = getActivity().getIntent().getStringExtra("type").equals("pv");
    } catch (Exception ignored) {
    }
    if (isPv) {
      bio.setVisibility(View.GONE);
      title.setText(getActivity().getIntent().getStringExtra("fullname"));
      File httpCacheDirectory = new File(getActivity().getBaseContext().getCacheDir(), "picasso-cache");
      Cache cache = new Cache(httpCacheDirectory, 15 * 1024 * 1024);
      OkHttpClient.Builder okHttpClientBuilder = new OkHttpClient.Builder().cache(cache);
      picassoWithCache = new Picasso.Builder(getActivity().getBaseContext()).downloader(new OkHttp3Downloader(okHttpClientBuilder.build())).build();
      picassoWithCache.load(getActivity().getIntent().getStringExtra("image")).transform(new CropCircleTransformation()).into(image);

    } else {
      title.setText(getActivity().getIntent().getStringExtra("fullname"));
      bio.setText(getActivity().getIntent().getStringExtra("bio"));
    }

    final ImageView pushButton = getActivity().findViewById(R.id.push_button);
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

            pushButton.setBackgroundResource(R.drawable.ptt_pressed);
            PlumbleActivity.mService.onTalkKeyDown();
            break;


          case MotionEvent.ACTION_UP:
            //start and stop counter
            cmTimer.setBase(SystemClock.elapsedRealtime());
            cmTimer.stop();

            pushButton.setBackgroundResource(R.drawable.ptt_unpressed);
            PlumbleActivity.mService.onTalkKeyUp();
            break;
        }
        return true;
      }
    });



  }

  public void setMuteDeafenFirstTime() {

    final IJumbleSession session = PlumbleActivity.mService.getSession();
    IUser self = session.getSessionUser();
    final boolean deafened = false;
    session.setSelfMuteDeafState(deafened, deafened);
    if (deafened) {
      mute_image.setImageResource(R.drawable.speakermuted);
      final int sdk = android.os.Build.VERSION.SDK_INT;
      if(sdk < android.os.Build.VERSION_CODES.JELLY_BEAN) {
        mute_linear.setBackgroundDrawable(ContextCompat.getDrawable(getContext(), R.drawable.rounded_muted_speaker) );
      } else {
        mute_linear.setBackground(ContextCompat.getDrawable(getContext(), R.drawable.rounded_muted_speaker));
      }
//                    Snackbar
//                      .make(findViewById(android.R.id.content), "حالت بيصدا فعال شد", Snackbar.LENGTH_SHORT)
//                      .show();
    }
    else {
      mute_image.setImageResource(R.drawable.speaker);
      final int sdk = android.os.Build.VERSION.SDK_INT;
      if(sdk < android.os.Build.VERSION_CODES.JELLY_BEAN) {
        mute_linear.setBackgroundDrawable(ContextCompat.getDrawable(getContext(), R.drawable.rounded_speaker) );
      } else {
        mute_linear.setBackground(ContextCompat.getDrawable(getContext(), R.drawable.rounded_speaker));
      }
//                    Snackbar
//                      .make(findViewById(android.R.id.content), "حالت بيصدا غيرفعال شد", Snackbar.LENGTH_SHORT)
//                      .show();
    }
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
}