package com.morlunk.mumbleclient.app;

import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.morlunk.jumble.audio.AudioOutput;
import com.morlunk.mumbleclient.R;

public class LogActivity extends AppCompatActivity{

  public static String log = "";

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_log);

    final Button pushButton = findViewById(R.id.log_ptt);
    Button clear = findViewById(R.id.log_clear);
    final TextView log = findViewById(R.id.log);

    final Handler hn = new Handler();
    hn.postDelayed(new Runnable() {
                     @Override
                     public void run() {
                       log.setText(AudioOutput.log);
                       hn.postDelayed(this,100);
                     }
                   },200);

      pushButton.setOnTouchListener(new View.OnTouchListener() {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
          switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
              PlumbleActivity.mService.onTalkKeyDown();
              break;
            case MotionEvent.ACTION_UP:
              PlumbleActivity.mService.onTalkKeyUp();
              break;
          }

          return true;
        }
      });

    clear.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        log.setText("");
        AudioOutput.log="";
      }
    });

  }
}
