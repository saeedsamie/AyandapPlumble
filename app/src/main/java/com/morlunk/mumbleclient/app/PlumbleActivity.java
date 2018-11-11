/*
 * Copyright (C) 2014 Andrew Comminos
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.morlunk.mumbleclient.app;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.ActionBarDrawerToggle;
import android.text.InputType;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.morlunk.jumble.IJumbleService;
import com.morlunk.jumble.IJumbleSession;
import com.morlunk.jumble.model.IChannel;
import com.morlunk.jumble.model.Server;
import com.morlunk.jumble.protobuf.Mumble;
import com.morlunk.jumble.util.JumbleException;
import com.morlunk.jumble.util.JumbleObserver;
import com.morlunk.mumbleclient.BuildConfig;
import com.morlunk.mumbleclient.R;
import com.morlunk.mumbleclient.Settings;
import com.morlunk.mumbleclient.channel.ChannelFragment;
import com.morlunk.mumbleclient.channel.ServerInfoFragment;
import com.morlunk.mumbleclient.db.DatabaseCertificate;
import com.morlunk.mumbleclient.db.DatabaseProvider;
import com.morlunk.mumbleclient.db.PlumbleDatabase;
import com.morlunk.mumbleclient.db.PlumbleSQLiteDatabase;
import com.morlunk.mumbleclient.db.PublicServer;
import com.morlunk.mumbleclient.preference.PlumbleCertificateGenerateTask;
import com.morlunk.mumbleclient.preference.Preferences;
import com.morlunk.mumbleclient.servers.FavouriteServerListFragment;
import com.morlunk.mumbleclient.servers.ServerEditFragment;
import com.morlunk.mumbleclient.service.IPlumbleService;
import com.morlunk.mumbleclient.service.PlumbleService;
import com.morlunk.mumbleclient.util.JumbleServiceFragment;
import com.morlunk.mumbleclient.util.JumbleServiceProvider;
import com.morlunk.mumbleclient.util.PlumbleTrustStore;

import org.spongycastle.util.encoders.Hex;

import java.io.File;
import java.security.KeyStore;
import java.security.MessageDigest;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import info.guardianproject.netcipher.proxy.OrbotHelper;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;


public class PlumbleActivity extends AppCompatActivity implements
        FavouriteServerListFragment.ServerConnectHandler, JumbleServiceProvider, DatabaseProvider,
        SharedPreferences.OnSharedPreferenceChangeListener, DrawerAdapter.DrawerDataProvider,
        ServerEditFragment.ServerEditListener {
    /**
     * If specified, the provided integer drawer fragment ID is shown when the activity is created.
     */
    public static final String EXTRA_DRAWER_FRAGMENT = "drawer_fragment";
    public static IPlumbleService mService;
    public static String username;
    public static String userId;
    public static Context context;
    public static String plumbleUserName;
    public static ArrayList<IChannel> iChannels = new ArrayList<>();
    /**
     * List of fragments to be notified about service state changes.
     */

    SharedPreferences sharedPreferences;
    /**
     * List of fragments to be notified about service state changes.
     */
    ArrayList<IChannel> chls = new ArrayList<IChannel>();
    private boolean isCertificateCreated = false;
    private Server server;
    private PlumbleDatabase mDatabase;
    private Settings mSettings;
    private ActionBarDrawerToggle mDrawerToggle;
    private DrawerLayout mDrawerLayout;
    private DrawerAdapter mDrawerAdapter;
    private ProgressDialog mConnectingDialog;
    private AlertDialog mErrorDialog;
    private Fragment currentFragment;
    private List<JumbleServiceFragment> mServiceFragments = new ArrayList<JumbleServiceFragment>();
    private JumbleObserver mObserver = new JumbleObserver() {
        @Override
        public void onChannelAdded(IChannel channel) {
            if (mService.isConnected()) {
                updateChannelLists();
                Log.e("channel", "addded" + PlumbleActivity.iChannels.toString());
                super.onChannelAdded(channel);
            }
        }

        @Override
        public void onChannelRemoved(IChannel channel) {
            if (mService.isConnected()) {
                updateChannelLists();
                Log.e("channels", "removed" + PlumbleActivity.iChannels.toString());
                super.onChannelRemoved(channel);
            }
        }


        @Override
        public void onConnected() {
//            if (mSettings.shouldStartUpInPinnedMode()) {
////                loadDrawerFragment(DrawerAdapter.ITEM_PINNED_CHANNELS);
//            } else {
            loadDrawerFragment(DrawerAdapter.ITEM_RECENTS);
//            }
            updateChannelLists();
            registerUser();
            mDrawerAdapter.notifyDataSetChanged();
            supportInvalidateOptionsMenu();

            updateConnectionState(getService());
            setTitle(currentFragment.getTag());

        }

        @Override
        public void onConnecting() {
            updateConnectionState(getService());
            setTitle("Connecting..");
        }

        @Override
        public void onDisconnected(JumbleException e) {
            // Re-show server list if we're showing a fragment that depends on the service.
//            if (getSupportFragmentManager().findFragmentById(R.id.content_frame) instanceof JumbleServiceFragment) {
            loadDrawerFragment(DrawerAdapter.ITEM_RECENTS);
//            }
//            mDrawerAdapter.notifyDataSetChanged();
//            supportInvalidateOptionsMenu();

            updateConnectionState(getService());
            setTitle("اتصال قطع است");
        }

        @Override
        public void onTLSHandshakeFailed(X509Certificate[] chain) {
//            final Server lastServer = getService().getTargetServer();
            createCertificate(chain);
        }

        @Override
        public void onPermissionDenied(String reason) {
            AlertDialog.Builder adb = new AlertDialog.Builder(PlumbleActivity.this);
            adb.setTitle(R.string.perm_denied);
            adb.setMessage(reason);
            adb.show();
        }
    };
    private ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            setTitle(currentFragment.getTag());
            mService = ((PlumbleService.PlumbleBinder) service).getService();
            mService.setSuppressNotifications(true);
            mService.registerObserver(mObserver);
            mService.clearChatNotifications(); // Clear chat notifications on resume.

            mDrawerAdapter.notifyDataSetChanged();

            for (JumbleServiceFragment fragment : mServiceFragments)
                fragment.setServiceBound(true);

            // Re-show server list if we're showing a fragment that depends on the service.
//            if (getSupportFragmentManager().findFragmentById(R.id.content_frame) instanceof JumbleServiceFragment &&
//                    !mService.isConnected()) {
            loadDrawerFragment(DrawerAdapter.ITEM_RECENTS);
//            }
            updateConnectionState(getService());

        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mService = null;
            setTitle("اتصال قطع است");
        }
    };
    public static void deleteCache(Context context) {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT) {
            File[] dirs = context.getExternalCacheDirs();
            try {
                for (int i = 0; i < dirs.length; i++) {
                    File dir = context.getCacheDir();
                    deleteDir(dir);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        try {
            File dir = context.getCacheDir();
            deleteDir(dir);
            File edir = context.getExternalCacheDir();
            deleteDir(edir);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static boolean deleteDir(File dir) {
        if (dir != null && dir.isDirectory()) {
            String[] children = dir.list();
            for (int i = 0; i < children.length; i++) {
                boolean success = deleteDir(new File(dir, children[i]));
                if (!success) {
                    return false;
                }
            }
            return dir.delete();
        } else if (dir != null && dir.isFile()) {
            return dir.delete();
        } else if (dir.isFile()) {
            return dir.delete();
        } else
            return false;

    }

    private void createCertificate(X509Certificate[] chain) {
        final Server lastServer = server;
        if (chain.length == 0)
            return;
        try {
            final X509Certificate x509 = chain[0];
            AlertDialog.Builder adb = new AlertDialog.Builder(PlumbleActivity.this);
            adb.setTitle(R.string.untrusted_certificate);
            try {
                MessageDigest digest = MessageDigest.getInstance("SHA-1");
                byte[] certDigest = digest.digest(x509.getEncoded());
                String hexDigest = new String(Hex.encode(certDigest));
                adb.setMessage(getString(R.string.certificate_info,
                        x509.getSubjectDN().getName(),
                        x509.getNotBefore().toString(),
                        x509.getNotAfter().toString(),
                        hexDigest));
            } catch (Exception e) {
                e.printStackTrace();
                adb.setMessage(x509.toString());
            }
            adb.setPositiveButton(R.string.allow, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
//                 Try to add to trust store
                    try {
                        String alias = lastServer.getHost();
                        KeyStore trustStore = PlumbleTrustStore.getTrustStore(PlumbleActivity.this);
                        trustStore.setCertificateEntry(alias, x509);
                        PlumbleTrustStore.saveTrustStore(PlumbleActivity.this, trustStore);
                        Toast.makeText(PlumbleActivity.this, R.string.trust_added, Toast.LENGTH_LONG).show();
                        isCertificateCreated = true;
                        connectToServer(lastServer);
                    } catch (Exception e) {
                        e.printStackTrace();
                        isCertificateCreated = false;
                        Toast.makeText(PlumbleActivity.this, R.string.trust_add_failed, Toast.LENGTH_LONG).show();
                    }
                }
            });
//            adb.setNegativeButton(R.string.wizard_cancel, null);
            adb.setCancelable(true);
            adb.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    ArrayList<IChannel> updateChannelLists() {

        chls = new ArrayList<IChannel>();
        if (chls.size() == 0) {
            chls.add(mService.getSession().getChannel(0));
        }
        for (int i = 0; i < chls.size(); i++) {
            IChannel channel = mService.getSession().getChannel(chls.get(i).getId());
            if (channel != null) {
//                constructNodes(null, channel, 0, mNodes);
                iChannels.add(channel);
                chls.addAll(channel.getSubchannels());
            }
        }
        return iChannels;
    }

    public IPlumbleService getPlumbleService() {
        return mService;
    }

    public Settings getmSettings() {
        return mSettings;
    }

    public PlumbleDatabase getmDatabase() {
        return mDatabase;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        context = this;
        sharedPreferences = getSharedPreferences("MyPref", Context.MODE_PRIVATE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            getWindow().getDecorView().setLayoutDirection(View.LAYOUT_DIRECTION_RTL);
        }
        username = sharedPreferences.getString(getString(R.string.PREF_TAG_username), "DEFAULT Username");
        plumbleUserName = username + new Random().nextInt();
        userId = sharedPreferences.getString(getString(R.string.PREF_TAG_userid), "UserId");
        Log.e("ENTERED", "Plumble Activity ----- OnCreate");
        server = new Server
                (new Random().nextInt(), "MUMBLE-server", "31.184.132.206", 64738,
                        plumbleUserName, "");


        mSettings = Settings.getInstance(this);
        setTheme(mSettings.getTheme());

        setContentView(R.layout.activity_main);

        setStayAwake(mSettings.shouldStayAwake());

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        preferences.registerOnSharedPreferenceChangeListener(this);


        mDatabase = new PlumbleSQLiteDatabase(this); // TODO add support for cloud storage
        mDatabase.open();

        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        ListView mDrawerList = (ListView) findViewById(R.id.left_drawer);


        mDrawerList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent;
                switch ((int) id) {
                    case DrawerAdapter.PROFILE_PROFILE:
                        intent = new Intent(PlumbleActivity.this, ProfileActivity.class);
//                        intent = new Intent(PlumbleActivity.this, SignupActivity.class);
//                        intent.putExtra("Launcher", "main");
//                        intent.putExtra(PlumbleActivity.this.getString(R.string.PREF_TAG_fullname),
//                                sharedPreferences.getString(PlumbleActivity.this.getString(R.string.PREF_TAG_fullname), "default full name"));
//                        intent.putExtra(PlumbleActivity.this.getString(R.string.PREF_TAG_username),
//                                sharedPreferences.getString(PlumbleActivity.this.getString(R.string.PREF_TAG_username), "default username"));
//                        startActivity(intent);
                        startActivity(intent);
                        break;
                }
//                mDrawerLayout.closeDrawers();
                loadDrawerFragment((int) id);
                mDrawerLayout.closeDrawers();
            }
        });

        mDrawerAdapter = new DrawerAdapter(this, this);
//        mDrawerAdapter.getView()
        mDrawerList.setAdapter(mDrawerAdapter);
        mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, R.string.drawer_open, R.string.drawer_close) {
            @Override
            public void onDrawerClosed(View drawerView) {
                supportInvalidateOptionsMenu();
            }

            @Override
            public void onDrawerStateChanged(int newState) {
                super.onDrawerStateChanged(newState);
                // Prevent push to talk from getting stuck on when the drawer is opened.
                if (getService() != null && getService().isConnected()) {
                    IJumbleSession session = getService().getSession();
                    if (session.isTalking() && !mSettings.isPushToTalkToggle()) {
                        session.setTalkingState(false);
                    }
                }
            }

            @Override
            public void onDrawerOpened(View drawerView) {
                supportInvalidateOptionsMenu();
            }
        };

        mDrawerLayout.setDrawerListener(mDrawerToggle);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        // Tint logo to theme
        int iconColor = getTheme().obtainStyledAttributes(new int[]{android.R.attr.textColorPrimaryInverse}).getColor(0, -1);
        Drawable logo = getResources().getDrawable(R.drawable.ic_home);
        logo.setColorFilter(iconColor, PorterDuff.Mode.MULTIPLY);
        getSupportActionBar().setLogo(logo);

        AlertDialog.Builder dadb = new AlertDialog.Builder(this);
        dadb.setMessage(R.string.disconnectSure);
        dadb.setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (mService != null) mService.disconnect();
//                loadDrawerFragment(DrawerAdapter.ITEM_SERVER);
            }
        });
        dadb.setNegativeButton(android.R.string.cancel, null);
//        mDisconnectPromptBuilder = dadb;

        if (savedInstanceState == null) {
//            if (getIntent() != null && getIntent().hasExtra(EXTRA_DRAWER_FRAGMENT)) {
//                loadDrawerFragment(getIntent().getIntExtra(EXTRA_DRAWER_FRAGMENT,
//                        DrawerAdapter.ITEM_RECENTS));
//            } else {
            loadDrawerFragment(DrawerAdapter.ITEM_RECENTS);
//            }
        }

//        // If we're given a Mumble URL to show, open up a server edit fragment.
//        if(getIntent() != null &&
//                Intent.ACTION_VIEW.equals(getIntent().getAction())) {
//            String url = getIntent().getDataString();
//            try {
//                Server server = MumbleURLParser.parseURL(url);
//
//                // Open a dialog prompting the user to connect to the Mumble server.
//                DialogFragment fragment = (DialogFragment) ServerEditFragment.createServerEditDialog(
//                        PlumbleActivity.this, server, ServerEditFragment.Action.CONNECT_ACTION, true);
//                fragment.show(getSupportFragmentManager(), "url_edit");
//            } catch (MalformedURLException e) {
//                Toast.makeText(this, getString(R.string.mumble_url_parse_failed), Toast.LENGTH_LONG).show();
//                e.printStackTrace();
//            }
//        }

        setVolumeControlStream(mSettings.isHandsetMode() ?
                AudioManager.STREAM_VOICE_CALL : AudioManager.STREAM_MUSIC);

        if (mSettings.isFirstRun()) showSetupWizard();

        connectToServer(server);


    }

    private void registerUser() {
        if (!sharedPreferences.getBoolean("isRegistered", false)) {
            if (getService() != null && getService().isConnected() && mSettings.isUsingCertificate() && isCertificateCreated) {
                ((PlumbleService) getService()).registerUser(getService().getSession().getSessionUser().getSession());
                if (sharedPreferences.edit().putBoolean("isRegistered", true).commit()) {
//                    Toast.makeText(this, "Registered :D !", Toast.LENGTH_LONG).show();
                } else {
//                    Toast.makeText(this, "CAN NOT REGISTER!", Toast.LENGTH_LONG).show();
                }
            }
        }
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        mDrawerToggle.syncState();
    }

    @Override
    protected void onResume() {
        super.onResume();
        Intent connectIntent = new Intent(this, PlumbleService.class);
        bindService(connectIntent, mConnection, 0);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mErrorDialog != null)
            mErrorDialog.dismiss();
        if (mConnectingDialog != null)
            mConnectingDialog.dismiss();

        if (mService != null) {
            for (JumbleServiceFragment fragment : mServiceFragments) {
                fragment.setServiceBound(false);
            }
            mService.unregisterObserver(mObserver);
            mService.setSuppressNotifications(false);
        }
        unbindService(mConnection);
    }

    @Override
    protected void onDestroy() {
//        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
//        preferences.unregisterOnSharedPreferenceChangeListener(this);
//        mDatabase.close();
        deleteCache(PlumbleActivity.this);
        super.onDestroy();
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
//        MenuItem disconnectButton = menu.findItem(R.id.action_disconnect);
//        disconnectButton.setVisible(mService != null && mService.isConnected());

        // Color the action bar icons to the primary text color of the theme.
        int foregroundColor = getSupportActionBar().getThemedContext()
                .obtainStyledAttributes(new int[]{android.R.attr.textColor})
                .getColor(0, -1);
        for (int x = 0; x < menu.size(); x++) {
            MenuItem item = menu.getItem(x);
            if (item.getIcon() != null) {
                Drawable icon = item.getIcon().mutate(); // Mutate the icon so that the color filter is exclusive to the action bar
                icon.setColorFilter(foregroundColor, PorterDuff.Mode.MULTIPLY);
            }
        }

        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.plumble, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (mDrawerToggle.onOptionsItemSelected(item))
            return true;
        Intent intent;
        switch (item.getItemId()) {
//            case R.id.action_disconnect:
//                getService().disconnect();
//                return true;
            case R.id.menu_requests:
                intent = new Intent(PlumbleActivity.this, RequestActivity.class);
                startActivity(intent);
                break;
        }
        return false;
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mDrawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (mService != null && keyCode == mSettings.getPushToTalkKey()) {
            mService.onTalkKeyDown();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if (mService != null && keyCode == mSettings.getPushToTalkKey()) {
            mService.onTalkKeyUp();
            return true;
        }
        return super.onKeyUp(keyCode, event);
    }

    @Override
    public void onBackPressed() {

        if (mDrawerLayout.isDrawerOpen(Gravity.RIGHT) || mDrawerLayout.isDrawerOpen(Gravity.LEFT)) {
            mDrawerLayout.closeDrawers();
        } else if (!currentFragment.getClass().getName().equals(RecentChatsFragment.class.getName())) {
            RecentChatsFragment recentChatsFragment = new RecentChatsFragment();
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.content_frame, recentChatsFragment, "صفحه ی اصلی")
                    .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                    .commit();
            setTitle("صفحه ی اصلی");
            setCurrentFragment(recentChatsFragment);
        } else {
            AlertDialog.Builder dadb = new AlertDialog.Builder(this);
            dadb.setMessage(R.string.leave_application_message);
            dadb.setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    if (mService != null) mService.disconnect();
                    finish();
                }
            });
            dadb.setNegativeButton(android.R.string.cancel, null);
            dadb.show();
//        super.onBackPressed();
        }
    }

    /**
     * Shows a nice looking setup wizard to guide the user through the app's settings.
     * Will do nothing if it isn't the first launch.
     */
    private void showSetupWizard() {
        // Prompt the user to generate a certificate.

        if (mSettings.isUsingCertificate()) return;
        AlertDialog.Builder adb = new AlertDialog.Builder(this);
//        adb.setTitle(R.string.first_run_generate_certificate_title);
//        adb.setMessage(R.string.first_run_generate_certificate);
//        adb.setPositiveButton(R.string.generate, new DialogInterface.OnClickListener() {
//            @Override
//            public void onClick(DialogInterface dialog, int which) {
        @SuppressLint("StaticFieldLeak") PlumbleCertificateGenerateTask generateTask = new PlumbleCertificateGenerateTask(PlumbleActivity.this) {
            @Override
            protected void onPostExecute(DatabaseCertificate result) {
                super.onPostExecute(result);
                if (result != null) mSettings.setDefaultCertificateId(result.getId());
//
            }
        };
        generateTask.execute();
//            }
//        });
//        adb.show();
        mSettings.setFirstRun(false);

        // TODO: finish wizard
//        Intent intent = new Intent(this, WizardActivity.class);
//        startActivity(intent);
    }

    public void setCurrentFragment(Fragment currentFragment) {
        this.currentFragment = currentFragment;
    }

    /**
     * Loads a fragment from the drawer.
     */
    private void loadDrawerFragment(int fragmentId) {
        boolean fragmentchecker = true;
        Class<? extends Fragment> fragmentClass = null;
        Bundle args = new Bundle();
        Intent intent;
        switch (fragmentId) {
            case DrawerAdapter.ITEM_SERVER:
                fragmentClass = ChannelFragment.class;
                break;
            case DrawerAdapter.ITEM_CREATE_CHAT:
                fragmentchecker = false;
                intent = new Intent(this, CreateChatActivity.class);
                intent.putExtra("plumbleUserName", plumbleUserName);
                startActivity(intent);
                break;
            case DrawerAdapter.ITEM_CREATE_GROUP:
                fragmentchecker = false;
                intent = new Intent(this, CreateGroupActivity.class);
                intent.putExtra("plumbleUserName", plumbleUserName);
                startActivity(intent);
                break;
            case DrawerAdapter.ITEM_INFO:
                fragmentClass = ServerInfoFragment.class;
                setTitle("اطلاعات");
                break;
//            case DrawerAdapter.ITEM_ACCESS_TOKENS:
//                fragmentClass = AccessTokenFragment.class;
//                Server connectedServer = getService().getTargetServer();
//                args.putLong("server", connectedServer.getId());
//                args.putStringArrayList("access_tokens", (ArrayList<String>) mDatabase.getAccessTokens(connectedServer.getId()));
//                break;
//            case DrawerAdapter.ITEM_PINNED_CHANNELS:
//                fragmentClass = ChannelFragment.class;
//                args.putBoolean("pinned", true);
//                break;
//            case DrawerAdapter.ITEM_FAVOURITES:
//                fragmentClass = FavouriteServerListFragment.class;
//                break;
            case DrawerAdapter.ITEM_CHAT:
                intent = new Intent(this, ChatActivity.class);
                startActivity(intent);
                return;
            case DrawerAdapter.ITEM_RECENTS:
                RecentChatsFragment recentChatsFragment = new RecentChatsFragment();
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.content_frame, recentChatsFragment, "صفحه ی اصلی")
                        .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                        .commit();
                setTitle("صفحه ی اصلی");
                setCurrentFragment(recentChatsFragment);
                return;
//            case DrawerAdapter.ITEM_PUBLIC:
//                fragmentClass = PublicServerListFragment.class;
//                break;
            case DrawerAdapter.ITEM_SETTINGS:
                Intent prefIntent = new Intent(this, Preferences.class);
                startActivity(prefIntent);
                return;
            case DrawerAdapter.EXIT:
                sharedPreferences.edit().clear().apply();
                PreferenceManager.getDefaultSharedPreferences(this).edit().clear().apply();
                PlumbleActivity.this.deleteDatabase("mumble.db");
                deleteCache(PlumbleActivity.this);
                mService.disconnect();
                this.finish();
                PlumbleTrustStore.clearTrustStore(this);
                Intent exitIntent = new Intent(this, LoginActivity.class);
                startActivity(exitIntent);
                return;
            default:
                return;
        }
        if (fragmentchecker) {
            Fragment fragment = Fragment.instantiate(this, fragmentClass.getName(), args);
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.content_frame, fragment, fragmentClass.getName())
                    .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                    .commit();
            setCurrentFragment(fragment);
        }
//        setTitle(mDrawerAdapter.getItemWithId(fragmentId).title);
    }

    public void connectToServer(final Server server) {
        // Check if we're already connected to a server; if so, inform user.
        if (mService != null && mService.isConnected()) {
            AlertDialog.Builder adb = new AlertDialog.Builder(this);
            adb.setMessage(R.string.reconnect_dialog_message);
            adb.setPositiveButton(R.string.connect, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    // Register an observer to reconnect to the new server once disconnected.
                    mService.registerObserver(new JumbleObserver() {
                        @Override
                        public void onDisconnected(JumbleException e) {
                            connectToServer(server);
                            mService.unregisterObserver(this);
                        }
                    });
                    mService.disconnect();
                }
            });
            adb.setNegativeButton(android.R.string.cancel, null);
            adb.show();
            return;
        }

        // Prompt to start Orbot if enabled but not running
        // TODO(acomminos): possibly detect onion address before connecting?
        if (mSettings.isTorEnabled()) {
            if (!OrbotHelper.isOrbotRunning(this)) {
                OrbotHelper.requestShowOrbotStart(this);
                return;
            }
        }

        ServerConnectTask connectTask = new ServerConnectTask(this, mDatabase);
        connectTask.execute(server);
    }

    @Override
    public void connectToPublicServer(final PublicServer server) {
//        AlertDialog.Builder alertBuilder = new AlertDialog.Builder(this);
//
//        final Settings settings = Settings.getInstance(this);
//
//        // Allow username entry
//        final EditText usernameField = new EditText(this);
//        usernameField.setHint(settings.getDefaultUsername());
//        alertBuilder.setView(usernameField);
//
//        alertBuilder.setTitle(R.string.connectToServer);
//
//        alertBuilder.setPositiveButton(R.string.connect, new DialogInterface.OnClickListener() {
//            @Override
//            public void onClick(DialogInterface dialog, int which) {
//                PublicServer newServer = server;
//                if (!usernameField.getText().toString().equals(""))
//                    newServer.setUsername(usernameField.getText().toString());
//                else
//                    newServer.setUsername(settings.getDefaultUsername());
//                connectToServer(newServer);
//            }
//        });

//        alertBuilder.show();
    }

    /*
     * HERE BE IMPLEMENTATIONS
     */

    private void setStayAwake(boolean stayAwake) {
        if (stayAwake) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        } else {
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        }
    }

    /**
     * Updates the activity to represent the connection state of the given service.
     * Will show reconnecting dialog if reconnecting, dismiss otherwise, etc.
     * Basically, this service will do catch-up if the activity wasn't bound to receive
     * connection state updates.
     *
     * @param service A bound IJumbleService.
     */
    @SuppressLint({"StringFormatMatches", "StringFormatInvalid"})
    private void updateConnectionState(IJumbleService service) {
        if (mConnectingDialog != null)
            mConnectingDialog.dismiss();
        if (mErrorDialog != null)
            mErrorDialog.dismiss();

        switch (mService.getConnectionState()) {
            case CONNECTING:
                setTitle("Connecting");
//                Server server = service.getTargetServer();
                mConnectingDialog = new ProgressDialog(this);
                mConnectingDialog.setIndeterminate(true);
                mConnectingDialog.setCancelable(false);
                mConnectingDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialog) {
                        mService.disconnect();
                        Toast.makeText(PlumbleActivity.this, R.string.cancelled,
                                Toast.LENGTH_SHORT).show();
                    }
                });
                mConnectingDialog.setMessage(getString(R.string.connecting_to_server, "",
                        ""));
                mConnectingDialog.show();
                break;
            case CONNECTION_LOST:
                // Only bother the user if the error hasn't already been shown.
                setTitle("اتصال قطع است");
                if (!getService().isErrorShown()) {
                    JumbleException error = getService().getConnectionError();
                    AlertDialog.Builder ab = new AlertDialog.Builder(PlumbleActivity.this);
                    ab.setTitle(R.string.connectionRefused);
                    if (mService.isReconnecting()) {
                        try {
                            ab.setMessage(getString(R.string.attempting_reconnect, error.getMessage()));
                        } catch (Exception e) {
                            e.printStackTrace();
                            ab.setMessage(getString(R.string.attempting_reconnect, "we dont know!"));

                        }
                        ab.setPositiveButton(R.string.cancel_reconnect, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if (getService() != null) {
                                    getService().cancelReconnect();
                                    getService().markErrorShown();
                                }
                            }
                        });
                    } else if (error.getReason() == JumbleException.JumbleDisconnectReason.REJECT &&
                            (error.getReject().getType() == Mumble.Reject.RejectType.WrongUserPW ||
                                    error.getReject().getType() == Mumble.Reject.RejectType.WrongServerPW)) {
                        // FIXME(acomminos): Long conditional.
                        final EditText passwordField = new EditText(this);
                        passwordField.setInputType(InputType.TYPE_CLASS_TEXT |
                                InputType.TYPE_TEXT_VARIATION_PASSWORD);
                        passwordField.setHint(R.string.password);
                        ab.setTitle(R.string.invalid_password);
                        ab.setMessage(error.getMessage());
                        ab.setView(passwordField);
                        ab.setPositiveButton(R.string.reconnect, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
//                                Server server = getService().getTargetServer();
//                                server = getService().getTargetServer();
                                if (server == null)
                                    return;
                                String password = passwordField.getText().toString();
                                server.setPassword(password);
                                if (server.isSaved())
                                    mDatabase.updateServer(server);
                                connectToServer(server);
                            }
                        });
                        ab.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if (getService() != null)
                                    getService().markErrorShown();
                            }
                        });
                    } else {
                        ab.setMessage(error.getMessage());
                        ab.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if (getService() != null)
                                    getService().markErrorShown();
                            }
                        });
                    }
                    ab.setCancelable(false);
                    mErrorDialog = ab.show();
                }
                break;
        }
    }

    @Override
    public IPlumbleService getService() {
        return mService;
    }

    @Override
    public PlumbleDatabase getDatabase() {
        return mDatabase;
    }

    @Override
    public void addServiceFragment(JumbleServiceFragment fragment) {
        mServiceFragments.add(fragment);
    }

    @Override
    public void removeServiceFragment(JumbleServiceFragment fragment) {
        mServiceFragments.remove(fragment);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (Settings.PREF_THEME.equals(key)) {
            // Recreate activity when theme is changed
//            recreate();
//        } else if (Settings.PREF_STAY_AWAKE.equals(key)) {
//            setStayAwake(mSettings.shouldStayAwake());
//        } else if (Settings.PREF_HANDSET_MODE.equals(key)) {
//            setVolumeControlStream(mSettings.isHandsetMode() ?
//                    AudioManager.STREAM_VOICE_CALL : AudioManager.STREAM_MUSIC);
        }
    }

    @Override
    public boolean isConnected() {
        return mService != null && mService.isConnected();
    }

    @Override
    public String getConnectedServerName() {
        if (mService != null && mService.isConnected()) {
//            Server server = mService.getTargetServer();
            return server.getName().equals("") ? server.getHost() : server.getName();
        }
        if (BuildConfig.DEBUG)
            throw new RuntimeException("getConnectedServerName should only be called if connected!");
        return "";
    }

    @Override
    public void onServerEdited(ServerEditFragment.Action action, Server server) {
//        switch (action) {
//            case ADD_ACTION:
//                mDatabase.addServer(server);
//                loadDrawerFragment(DrawerAdapter.ITEM_FAVOURITES);
//                break;
//            case EDIT_ACTION:
//                mDatabase.updateServer(server);
//                loadDrawerFragment(DrawerAdapter.ITEM_FAVOURITES);
//                break;
//            case CONNECT_ACTION:
//                connectToServer(server);
//                break;
//        }
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }
}
