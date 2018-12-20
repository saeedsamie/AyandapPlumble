package com.morlunk.ayandap.app;

import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.morlunk.ayandap.R;
import com.morlunk.jumble.audio.AudioOutput;
import com.morlunk.jumble.util.JumbleDisconnectedException;

import static com.morlunk.ayandap.app.PlumbleActivity.mService;

public class LogActivity extends AppCompatActivity {

  public static String log = "";

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_log);
    final TextView textView = findViewById(R.id.server_TCP_ping_log);

    final Button pushButton = findViewById(R.id.log_ptt);
    Button clear = findViewById(R.id.log_clear);
    final TextView log = findViewById(R.id.log);

    final Handler handler = new Handler();
    handler.postDelayed(new Runnable() {
      @Override
      public void run() {
        long l = 0;
        try {
          l = PlumbleActivity.l;
        } catch (JumbleDisconnectedException e) {
        }
        log.setText(AudioOutput.log);
        if(l<200)
          textView.setBackgroundColor(Color.GREEN);
        else if(l<500)
          textView.setBackgroundColor(Color.YELLOW);
        else if(500<l)
          textView.setBackgroundColor(Color.RED);

        textView.setText( l + "ms");
        handler.postDelayed(this, 10);
      }
    }, 0);




      pushButton.setOnTouchListener(new View.OnTouchListener() {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
          switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
              mService.onTalkKeyDown();
              long time = System.currentTimeMillis();
              AudioOutput.log += time + "\n";
              break;
            case MotionEvent.ACTION_UP:
              mService.onTalkKeyUp();
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
