package com.morlunk.mumbleclient.app;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.view.View;
import android.widget.TextView;

import com.morlunk.mumbleclient.R;

import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

import static com.morlunk.mumbleclient.R.*;

public class ChatActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().getDecorView().setLayoutDirection(View.LAYOUT_DIRECTION_RTL);
        setContentView(R.layout.activity_chat);
        TextView textView = findViewById(R.id.chat_title);
        textView.setText(getIntent().getStringExtra("ChatId")+"\n"+getIntent().getStringExtra("ChatTitle"));
        setContentView(layout.activity_chat);
        TextView textView = findViewById(id.chat_title);
        String chatId = getIntent().getStringExtra("ChatId");
        String chatTitle = getIntent().getStringExtra("ChatTitle");
        if ((chatId != null && chatTitle != null))
            textView.setText(chatId + "\n" + chatTitle);
        LinearLayout linearLayout = findViewById(id.chat_header);
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
            layoutParams.setMargins(5,5,5,5);
            imageButton.setPadding(3, 3, 3, 3);
            linearLayout.addView(imageButton, layoutParams);
        }
//        FrameLayout pushButton =(FrameLayout)findViewById(id.push_button);
    }




    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }
}
