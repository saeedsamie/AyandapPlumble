package com.morlunk.mumbleclient.app;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.widget.TextView;

import com.morlunk.mumbleclient.R;

import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class ChatActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ;
        setContentView(R.layout.activity_chat);
        TextView textView = findViewById(R.id.chat_title);
        textView.setText(getIntent().getStringExtra("ChatId")+"\n"+getIntent().getStringExtra("ChatTitle"));
    }




    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }
}
