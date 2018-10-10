package com.morlunk.mumbleclient.app;

import android.app.ActivityOptions;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.morlunk.mumbleclient.OnTaskCompletedListener;
import com.morlunk.mumbleclient.R;
import com.morlunk.mumbleclient.ServerFetchAsync;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;


public class LoginActivity extends AppCompatActivity implements OnTaskCompletedListener {

    public static final int REQUEST_EXIT = 22;
    public static SharedPreferences sharedPreferences;
    public static String user_phone_number;
    public Button loginContinue;
    public EditText phone;
    private boolean isUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        sharedPreferences = getSharedPreferences("MyPref", Context.MODE_PRIVATE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            getWindow().getDecorView().setLayoutDirection(View.LAYOUT_DIRECTION_RTL);
        }
        if (sharedPreferences.getBoolean("isLoggedIn", false)) {
//            SignupActivity.userId = sharedPreferences.getString("userId", null);
            Intent intent = new Intent(LoginActivity.this, PlumbleActivity.class);
            startActivity(intent);
            finish();
        }

        super.onCreate(savedInstanceState);
        setContentView(R.layout.login);

        loginContinue = (Button) findViewById(R.id.login_continue);
        phone = (EditText) findViewById(R.id.login_phone);


        final LoginActivity loginActivity = this;

        loginContinue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
                nameValuePairs.add(new BasicNameValuePair("func", "isUser"));
                user_phone_number = phone.getText().toString();
                nameValuePairs.add(new BasicNameValuePair("phone", user_phone_number));
                new ServerFetchAsync(nameValuePairs, loginActivity).execute();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_EXIT)
            if (resultCode == RESULT_OK) {
                this.finish();
            }
    }

    @Override
    public void onTaskCompleted(JSONObject jsonObject) {


        try {
            isUser = jsonObject.getJSONArray("resLogin").getJSONObject(0).getString("isUser").equals("1");
            SharedPreferences.Editor editor = sharedPreferences.edit();

//            editor.putBoolean(getString(R.string.PREF_TAG_isLoggedIn), true);

            String fullname = null;
            String username = null;
            String image = null;
            String userId = null;
            try {
                fullname = jsonObject.getJSONArray("resLogin").getJSONObject(0).getString("fullName");
                username = jsonObject.getJSONArray("resLogin").getJSONObject(0).getString("userName");
                image = jsonObject.getJSONArray("resLogin").getJSONObject(0).getString("image");
                userId = jsonObject.getJSONArray("resLogin").getJSONObject(0).getString("userId");
                editor.putString(getString(R.string.PREF_TAG_phonenumber), user_phone_number);
                editor.putString(getString(R.string.PREF_TAG_fullname), fullname);
                editor.putString(getString(R.string.PREF_TAG_username), username);
                editor.putString(getString(R.string.PREF_TAG_image), image);
                editor.putString(getString(R.string.PREF_TAG_userid), userId);
//                SignupActivity.userId = userId;
            } catch (Exception e) {
                e.printStackTrace();
            }
            Boolean isSavedInPref = editor.commit();
            if (isSavedInPref) {
                Intent intent = new Intent(LoginActivity.this, VerificationActivity.class);
                intent.putExtra("isUser", isUser);
                intent.putExtra("Phone_number", user_phone_number);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    startActivityForResult(intent, REQUEST_EXIT, ActivityOptions.makeCustomAnimation(this, R.anim.fade_in, R.anim.fade_out).toBundle());
                } else
                    startActivityForResult(intent, REQUEST_EXIT);
            } else {
                AlertDialog.Builder alertBuilder = new AlertDialog.Builder(LoginActivity.this);
                alertBuilder.setMessage("ثبت نشد!" + "\n" + "دوباره سعی کنید!");
                alertBuilder.setCancelable(false);
                alertBuilder.setNeutralButton("باشه", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                });
                alertBuilder.show();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }
}