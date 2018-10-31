package com.morlunk.mumbleclient.app;

import android.app.Activity;
import android.app.ActivityOptions;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

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
    public static String URL = "http://31.184.132.206/";
    public Button loginContinue;
    public EditText phone;
    private boolean isUser;
    private FrameLayout progressBarPage;
    private LinearLayout linearLayout;
    private LinearLayout loginPanel;
    private LinearLayout loginBasePanel;
    private TextView errorBox;

    public static void hideKeyboard(Activity activity) {
        InputMethodManager imm = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
        //Find the currently focused view, so we can grab the correct window token from it.
        View view = activity.getCurrentFocus();
        //If no view currently has focus, create a new one, just so we can grab a window token from it
        if (view == null) {
            view = new View(activity);
        }
        if (imm != null) {
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        sharedPreferences = getSharedPreferences("MyPref", Context.MODE_PRIVATE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            getWindow().getDecorView().setLayoutDirection(View.LAYOUT_DIRECTION_RTL);
        }
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
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
        phone.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public synchronized void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() == 11) {
                    hideKeyboard(LoginActivity.this);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
        errorBox = (TextView) findViewById(R.id.error_box);
        progressBarPage = findViewById(R.id.progress_bar_page);
        loginPanel = findViewById(R.id.login_panel);
        final LoginActivity loginActivity = this;
        loginContinue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                errorBox.setText("");
                progressBarPage.setVisibility(View.VISIBLE);
                loginPanel.setAlpha(0);

                ConnectivityManager ConnectionManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
                NetworkInfo networkInfo = null;
                if (ConnectionManager != null) {
                    networkInfo = ConnectionManager.getActiveNetworkInfo();
                }
                if (networkInfo != null && networkInfo.isConnected()) {
                    if (phone.getText().toString().equals("")) {
                        Snackbar.make(findViewById(android.R.id.content), "لطفا تلفن همراه خود را وارد نمایید", Snackbar.LENGTH_SHORT)
                                .show();
                    } else if (phone.getText().toString().length() < 11 && phone.getText().toString().length() > 0) {
                        Snackbar.make(findViewById(android.R.id.content), "لطفا تلفن همراه خود را به صورت کامل وارد نمایید", Snackbar.LENGTH_SHORT)
                                .show();
                    } else if (phone.getText().toString().length() == 11) {
                        String temp = phone.getText().toString().substring(0, 2);
                        if (!temp.equals("09")) {
                            Snackbar.make(findViewById(android.R.id.content), "لطفا تلفن همراه خود را به صورت صحیح وارد نمایید", Snackbar.LENGTH_SHORT)
                                    .show();
                        } else {

                            List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
                            nameValuePairs.add(new BasicNameValuePair("func", "isUser"));
                            user_phone_number = phone.getText().toString();
                            nameValuePairs.add(new BasicNameValuePair("phone", user_phone_number));
                            new ServerFetchAsync(nameValuePairs, loginActivity).execute();
                        }
                    }
                } else {
                    Toast.makeText(LoginActivity.this, "اتصال به شبکه را چک کنید!", Toast.LENGTH_LONG).show();
                    errorBox.setText("اتصال به شبکه را چک کنید!");
                }
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

        progressBarPage.setVisibility(View.GONE);
        loginPanel.setAlpha(1);

        try {
            if (jsonObject.getString("response").equals("TimeOut")) {
                Toast.makeText(LoginActivity.this, "پیامی از سرور دریافت نشد!", Toast.LENGTH_LONG).show();
                errorBox.setText("پیامی از سرور دریافت نشد!");
            } else {
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
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                        startActivityForResult(intent, REQUEST_EXIT, ActivityOptions.makeCustomAnimation(this, R.anim.fade_in, R.anim.fade_out).toBundle());
                    }
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