package com.morlunk.ayandap.app;

import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;

import com.morlunk.ayandap.R;
import com.morlunk.ayandap.calligraphy.CalligraphyContextWrapper;
import com.morlunk.jumble.IJumbleService;

import java.util.ArrayList;
import java.util.List;

public class ChatActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private TabLayout tabLayout;
    private ViewPager viewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            getWindow().getDecorView().setLayoutDirection(View.LAYOUT_DIRECTION_RTL);
        }

        int chatId = getIntent().getIntExtra("chatId", -1);
        String chatTitle = getIntent().getStringExtra("ChatTitle");
        setTitle(getIntent().getStringExtra("fullname"));
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

      viewPager = (ViewPager) findViewById(R.id.viewpager);
        setupViewPager(viewPager);

        tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(viewPager);

    }


    private void setupViewPager(ViewPager viewPager) {
        ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager());
        adapter.addFragment(new SendVoiceFragment(), "Chat");
        adapter.addFragment(new VoicesFragment(), "Voices");
        viewPager.setAdapter(adapter);
    }

    class ViewPagerAdapter extends FragmentPagerAdapter {
        private final List<Fragment> mFragmentList = new ArrayList<>();
        private final List<String> mFragmentTitleList = new ArrayList<>();

        public ViewPagerAdapter(FragmentManager manager) {
            super(manager);
        }



        @Override
        public Fragment getItem(int position) {
            return mFragmentList.get(position);
        }

        @Override
        public int getCount() {
            return mFragmentList.size();
        }

        public void addFragment(Fragment fragment, String title) {
            mFragmentList.add(fragment);
            mFragmentTitleList.add(title);
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return mFragmentTitleList.get(position);
        }
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

  @Override
  public void onPointerCaptureChanged(boolean hasCapture) {

  }

  @Override
  protected void attachBaseContext(Context newBase) {
    super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
  }

}