package com.morlunk.mumbleclient.app;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.morlunk.jumble.IJumbleService;
import com.morlunk.jumble.IJumbleSession;
import com.morlunk.jumble.util.JumbleDisconnectedException;
import com.morlunk.mumbleclient.R;
import com.morlunk.mumbleclient.service.IPlumbleService;

import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

import static com.morlunk.mumbleclient.R.drawable;
import static com.morlunk.mumbleclient.R.id;

public class ChatActivity extends ActionBarActivity {

    PlumbleActivity plumbleActivity;
    private ProgressDialog mConnectingDialog;
    private AlertDialog mErrorDialog;
    private IPlumbleService service;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            getWindow().getDecorView().setLayoutDirection(View.LAYOUT_DIRECTION_RTL);
        }
        setContentView(R.layout.activity_chat);
        TextView textView = (TextView) findViewById(id.chat_title);
        int chatId = getIntent().getIntExtra("chatId",-1);
        String chatTitle = getIntent().getStringExtra("ChatTitle");
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
            ImageButton imageButton = new ImageButton(this);

            imageButton.setImageResource(drawable.ic_launcher);
            //todo get image from server!

            imageButton.setBackgroundResource(drawable.round_button);
            imageButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                }
            });
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(150, 150);
            layoutParams.setMargins(5, 5, 5, 5);
            imageButton.setPadding(3, 3, 3, 3);
            linearLayout.addView(imageButton, layoutParams);
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
                LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(150, 150);
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
}
