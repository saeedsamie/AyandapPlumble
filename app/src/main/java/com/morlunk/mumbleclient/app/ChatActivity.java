package com.morlunk.mumbleclient.app;

import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.morlunk.mumbleclient.R;

import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

import static com.morlunk.mumbleclient.R.drawable;
import static com.morlunk.mumbleclient.R.id;

public class ChatActivity extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            getWindow().getDecorView().setLayoutDirection(View.LAYOUT_DIRECTION_RTL);
        }
        setContentView(R.layout.activity_chat);
        TextView textView = (TextView) findViewById(id.chat_title);
        String chatId = getIntent().getStringExtra("ChatId");
        String chatTitle = getIntent().getStringExtra("ChatTitle");
        if ((chatId != null && chatTitle != null)){
//            textView.setText(chatId + "\n" + chatTitle);
        }
        chatTitle = "ChatTitle";
        this.setTitle(chatTitle);
        LinearLayout linearLayout = (LinearLayout) findViewById(id.chat_header);
        linearLayout.setOrientation(LinearLayout.HORIZONTAL);
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
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(100, 100);
            layoutParams.setMargins(5, 5, 5, 5);
            imageButton.setPadding(3, 3, 3, 3);
            linearLayout.addView(imageButton, layoutParams);
        }
        final ImageButton pushButton = (ImageButton) findViewById(id.push_button);
        final int width =400;
        final int hight = 400;
//        pushButton.setOnTouchListener(new View.OnTouchListener() {
//            @Override
//            public boolean onTouch(View v, MotionEvent event) {
//                ResizeAnimation resizeAnimation;
//                switch (event.getAction()) {
//                    case MotionEvent.ACTION_DOWN:
////                        getService().onTalkKeyDown();
//                        break;
//                    case MotionEvent.ACTION_UP:
//
////                        getService().onTalkKeyUp();
//                        break;
//                }
//                return true;
//            }
//        });
    }


    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }
}
