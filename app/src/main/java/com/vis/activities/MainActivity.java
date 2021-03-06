/*
 * Copyright 2012 Google Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.vis.activities;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.annotation.TargetApi;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.util.Patterns;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.facebook.FacebookSdk;
import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.appindexing.Thing;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.android.youtube.player.YouTubeApiServiceUtil;
import com.google.android.youtube.player.YouTubeInitializationResult;
import com.google.gson.Gson;
import com.vis.AlarmReceiver;
import com.vis.Analytics;
import com.vis.FacebookActivity;
import com.vis.R;
import com.vis.adapters.PageAdapterForRecycler;
import com.vis.beans.AppActive;
import com.vis.beans.FbProfile;
import com.vis.beans.Registration;
import com.vis.beans.VideoEntry;
import com.vis.utilities.Constants;
import com.vis.utilities.GenericScrollListener;
import com.vis.utilities.Utility;
import com.vis.utilities.WebServiceUtility;

import org.ksoap2.SoapEnvelope;
import org.ksoap2.serialization.PropertyInfo;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapPrimitive;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpTransportSE;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Pattern;


/**
 * A sample Activity showing how to manage multiple YouTubeThumbnailViews in an adapter for display
 * in a List. When the list items are clicked, the video is played by using a YouTubePlayerFragment.
 * <p/>
 * The demo supports custom fullscreen and transitioning between portrait and landscape without
 * rebuffering.
 */
@TargetApi(13)
public class MainActivity extends AppCompatActivity {


    private static final int RECOVERY_DIALOG_REQUEST = 1;
    private RecyclerView recyclerView;
    String SENDER_ID = "995587742942";
    private boolean isFullscreen;
    GoogleCloudMessaging gcm;
    AtomicInteger msgId = new AtomicInteger();
    private PendingIntent pendingIntent;
    RelativeLayout noInternetMessage, listViewContainer;
    Context mContext;
    String regid;
    Button refreshButton;
    Utility utility;
    FbProfile fbProfile;
    BroadcastReceiver broadcast_reciever;
    SharedPreferences prefs;
    Toolbar toolbar;
    CollapsingToolbarLayout collapsingToolbar;
    AppBarLayout appBarLayout;
    PageAdapterForRecycler adapter;
    private LinearLayoutManager mLayoutManager;
    private Toolbar mToolbar;
    private List<VideoEntry> mainList;
    private Uri mUrl;
    private String mTitle;
    private String mDescription;
    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    private GoogleApiClient client;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FacebookSdk.sdkInitialize(getApplicationContext());
        setContentView(R.layout.video_list_demo);

        mUrl = Uri.parse("http://videoinshort.com/video");
        mTitle = "Video In Short";
        mDescription = "Funny videos App";
        utility = new Utility(this);

        noInternetMessage = (RelativeLayout) findViewById(R.id.no_internet_message);
        listViewContainer = (RelativeLayout) findViewById(R.id.list_view_container);
        refreshButton = (Button) findViewById(R.id.refreshButton);


        //getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        GoogleAnalytics.getInstance(this).reportActivityStart(this);
        initToolbar();

        mContext = this;
        prefs = getSharedPreferences(Constants.PREFERENCES_NAME, MODE_PRIVATE);
        recyclerView = (RecyclerView) findViewById(R.id.list_fragment);

        String responseFromFb = getIntent().getStringExtra(Constants.FB_USER_INFO);
        if (responseFromFb != null && !responseFromFb.isEmpty()) {
            fbProfile = new Gson().fromJson(responseFromFb, FbProfile.class);
        }

        if (checkPlayServices()) {
            gcm = GoogleCloudMessaging.getInstance(this);
            regid = getRegistrationId(mContext);

            if (regid.isEmpty()) {
                registerInBackground();
            } else {
                callAllRequiredWebservices();
                checkYouTubeApi();
            }
        } else {
            Log.i(Constants.TAG, "No valid Google Play Services APK found.");
        }


        boolean alarmAlreadySet = prefs.getBoolean("ALARM_SET", false);
        if (!alarmAlreadySet) {
            Intent alarmIntent = new Intent(MainActivity.this, AlarmReceiver.class);
            pendingIntent = PendingIntent.getBroadcast(MainActivity.this, 0, alarmIntent, 0);

            //System.out.println("ALARM CALLED ON CREATE");
            AlarmManager manager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
            int interval = 1000 * 60 * 60 * 24 * 7;
            int startTime = 1000 * 60 * 60 * 24 * 5;
            /*int interval = 1000 * 60 * 1;
                int startTime = 1000 * 60 * 1;*/
            /* Set the alarm to start at 10:30 AM */
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(System.currentTimeMillis() + startTime);
            /* Repeating on every 20 minutes interval */
            manager.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(),
                    interval, pendingIntent);
            prefs.edit().putBoolean("ALARM_SET", true).commit();
        }


        broadcast_reciever = new BroadcastReceiver() {

            @Override
            public void onReceive(Context arg0, Intent intent) {
                String action = intent.getAction();
                if (action.equals("finish_activity")) {
                    finish();
                    // DO WHATEVER YOU WANT.
                }
            }
        };
        refreshButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onRefresh();
            }
        });
        if (utility.checkInternetConnectivity()) {
            listViewContainer.setVisibility(View.VISIBLE);
            noInternetMessage.setVisibility(View.GONE);
            boolean isFirstTime = prefs.getBoolean(Constants.PREFERENCES_INTEREST, true);
            if (!isFirstTime) {
                loadListView();
            } else {
                Intent intent = new Intent(this, InterestActivity.class);
                startActivityForResult(intent, Constants.REQUEST_CODE_FOR_INTEREST);
            }

            // mainWebView.loadUrl(Constants.url);
        } else {
            listViewContainer.setVisibility(View.GONE);
            noInternetMessage.setVisibility(View.VISIBLE);
        }

        mLayoutManager = new LinearLayoutManager(MainActivity.this);
        recyclerView.setLayoutManager(mLayoutManager);
        adapter = new PageAdapterForRecycler(mContext, new ArrayList<VideoEntry>(), fbProfile);
        recyclerView.setAdapter(adapter);
        recyclerView.addOnScrollListener(new GenericScrollListener(mLayoutManager) {
            @Override
            public void onHide() {
                hideViews();
            }

            @Override
            public void onShow() {
                showViews();
            }

            @Override
            public void onLoadMore(int current_page) {

                int interimCount = adapter.getItemCount() + 33;
                if (interimCount < 103 && interimCount <= mainList.size()) {

                    adapter.setListEntries(mainList.subList(0, adapter.getItemCount() + 33));
                    adapter.notifyDataSetChanged();
                    recyclerView.requestLayout();


                }
                else  if(interimCount < 103 && interimCount > mainList.size() && adapter.getItemCount() < mainList.size())
                {
                    adapter.setListEntries(mainList.subList(0, mainList.size()));
                    adapter.notifyDataSetChanged();
                    recyclerView.requestLayout();
                }


            }
        });

        onDeepLinkIntent(getIntent());
        //  new WebServiceUtility(this,Constants.GET_VIDEOS,null);
        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();
    }

    public Action getAction() {
        Thing object = new Thing.Builder()
                .setName(mTitle)
                .setDescription(mDescription)
                .setUrl(mUrl)
                .build();

        return new Action.Builder(Action.TYPE_VIEW)
                .setObject(object)
                .setActionStatus(Action.STATUS_TYPE_COMPLETED)
                .build();
    }

    private void initToolbar() {
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        mToolbar.setTitle("");
       /* TextView title = (TextView) mToolbar.findViewById(R.id.toolbar_title);
        Typeface custom_font = Typeface.createFromAsset(getAssets(), "Calibri.ttf");
        title.setTypeface(custom_font);*/
        setSupportActionBar(mToolbar);
       /* Button feedbackButton = (Button) mToolbar.findViewById(R.id.button_feedback);
        feedbackButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, FeedbackActivity.class);
                intent.putExtra(Constants.MENU_SETTINGS, true);
                startActivity(intent);

            }
        });*/

    }

    private void callAllRequiredWebservices() {

        {
            SharedPreferences pref = getPreferences(mContext);
            String androidVersion = android.os.Build.VERSION.RELEASE;
            String deviceName = getDeviceName();
            String googleId = null;
            Pattern emailPattern = Patterns.EMAIL_ADDRESS; // API level 8+
            Account[] accounts = AccountManager.get(getApplicationContext()).getAccounts();
            for (Account account : accounts) {
                if (emailPattern.matcher(account.name).matches()) {
                    googleId = account.name;
                    break;
                }
            }
            if (fbProfile == null) {
                String responseFromFb = pref.getString(Constants.FB_USER_INFO, null);
                if (responseFromFb != null) {
                    fbProfile = new Gson().fromJson(responseFromFb, FbProfile.class);
                    if (fbProfile != null && fbProfile.getDeviceModel() == null) {
                        fbProfile.setDeviceModel(deviceName);
                        fbProfile.setOs(androidVersion);
                        fbProfile.setGoogleId(googleId);
                    }
                    new WebServiceUtility(getApplicationContext(), Constants.SEND_FACEBOOK_DATA, fbProfile);
                }
            } else {
                fbProfile.setDeviceModel(deviceName);
                fbProfile.setOs(androidVersion);
                fbProfile.setGoogleId(googleId);
                new WebServiceUtility(getApplicationContext(), Constants.SEND_FACEBOOK_DATA, fbProfile);
            }


            //Call execute
            String regId = getRegistrationId(mContext);
            String fbId = fbProfile.getFbUserId();
            String emailId = fbProfile.getEmail();
            if (regId != null && !regId.isEmpty() && fbId != null && !fbId.isEmpty() && emailId != null && !emailId.isEmpty()) {
                Registration registration = new Registration();
                registration.setEmailId(emailId);
                registration.setFbId(fbId);
                registration.setRegId(regId);
                registration.setAppVersion(getAppVersionName(getApplicationContext()));
                new WebServiceUtility(getApplicationContext(), Constants.USER_INFO_TASK, registration);
            }

            AppActive appActive = new AppActive();
            appActive.setRegId(regId);
            appActive.setNetworkConstant(utility.connectedNetwork());
            new WebServiceUtility(getApplicationContext(), Constants.SEND_APP_ACTIVE_DATA, appActive);
            checkForAppUpdateApp(getAppVersionName(getApplicationContext()));
        }
    }


    public String getDeviceName() {
        String manufacturer = Build.MANUFACTURER;
        String model = Build.MODEL;
        if (model.startsWith(manufacturer)) {
            return capitalize(model);
        } else {
            return capitalize(manufacturer) + " " + model;
        }
    }


    private String capitalize(String s) {
        if (s == null || s.length() == 0) {
            return "";
        }
        char first = s.charAt(0);
        if (Character.isUpperCase(first)) {
            return s;
        } else {
            return Character.toUpperCase(first) + s.substring(1);
        }
    }

    private void checkYouTubeApi() {
        YouTubeInitializationResult errorReason =
                YouTubeApiServiceUtil.isYouTubeApiServiceAvailable(this);
        if (errorReason.isUserRecoverableError()) {
            errorReason.getErrorDialog(this, RECOVERY_DIALOG_REQUEST).show();
        } else if (errorReason != YouTubeInitializationResult.SUCCESS) {
            String errorMessage =
                    String.format(getString(R.string.error_player), errorReason.toString());
            Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {


        if (requestCode == RECOVERY_DIALOG_REQUEST) {
            // Recreate the activity if user performed a recovery action
            recreate();
        } else if (requestCode == Constants.REQUEST_CODE_FOR_INTEREST) {
            loadListView();
        } else {
            super.onActivityResult(requestCode, resultCode, data);
            adapter.callbackManager.onActivityResult(requestCode, resultCode, data);
            recyclerView.requestLayout();

        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);


    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);


        String action = intent.getAction();
        String data = intent.getDataString();
        if (Intent.ACTION_VIEW.equals(action) && data != null) {

        } else {
            setIntent(intent);
            processIntent(getIntent());
        }


    }

    void onDeepLinkIntent(Intent intent) {
        String action = intent.getAction();
        String data = intent.getDataString();
        if (Intent.ACTION_VIEW.equals(action) && data != null) {
            String videoId = data.substring(data.lastIndexOf("/") + 1);

            showFullScreenVideo(videoId);
        }

       /* Branch branch = Branch.getInstance();
        branch.initSession(new Branch.BranchReferralInitListener() {
            @Override
            public void onInitFinished(JSONObject referringParams, BranchError error) {
                if (error == null) {
                    // params are the deep linked params associated with the link that the user clicked before showing up
                    Log.i("BranchConfigTest", "deep link data: " + referringParams.toString());
                }
            }
        }, intent.getData(), this);*/
    }

    void processIntent(Intent intent) {
        String notification = intent.getStringExtra("NOTIFICATION");
        if (notification != null && !notification.isEmpty()) {
            String regId = getRegistrationId(mContext);
            new WebServiceUtility(this, Constants.CLICK_INFO_TASK, notification);
            AppActive appActive = new AppActive();
            appActive.setRegId(regId);
            appActive.setNetworkConstant(utility.connectedNetwork());
            new WebServiceUtility(getApplicationContext(), Constants.SEND_APP_ACTIVE_DATA, appActive);
        }
        onRefresh();

    }

    public void onRefresh() {

        if (utility.checkInternetConnectivity()) {
            listViewContainer.setVisibility(View.VISIBLE);
            noInternetMessage.setVisibility(View.GONE);
            loadListView();

        } else {

            listViewContainer.setVisibility(View.GONE);
            noInternetMessage.setVisibility(View.VISIBLE);
        }
    }

    /**
     * @return Application's version code from the {@code PackageManager}.
     */
    private static int getAppVersion(Context context) {
        try {
            PackageInfo packageInfo = context.getPackageManager()
                    .getPackageInfo(context.getPackageName(), 0);
            return packageInfo.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            // should never happen
            throw new RuntimeException("Could not get package name: " + e);
        }
    }

    private static String getAppVersionName(Context context) {
        try {
            PackageInfo packageInfo = context.getPackageManager()
                    .getPackageInfo(context.getPackageName(), 0);
            return packageInfo.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            // should never happen
            throw new RuntimeException("Could not get package name: " + e);
        }
    }

    /**
     * @return Application's {@code SharedPreferences}.
     */
    private SharedPreferences getPreferences(Context context) {
        // This sample app persists the registration ID in shared preferences, but
        // how you store the regID in your app is up to you.
        return getSharedPreferences(Constants.PREFERENCES_NAME,
                Context.MODE_PRIVATE);
    }

    @Override
    protected void onStart() {
        // TODO Auto-generated method stub
        super.onStart();
        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client.connect();


        SharedPreferences pref = getPreferences(this);
        boolean showAlarm = pref.getBoolean(Constants.PREFERENCES_SHOW_ALARM, false);
        boolean alreadyRated = pref.getBoolean(Constants.PREFERENCES_ALREADY_RATED, false);
        if (showAlarm && !alreadyRated) {
            rateUs("You are awesome! If you feel the same about VideoInShort, please take a moment to rate it.");
        }
        GoogleAnalytics.getInstance(this).reportActivityStart(this);
        registerReceiver(broadcast_reciever, new IntentFilter("finish_activity"));
        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        /*Action viewAction = Action.newAction(
                Action.TYPE_VIEW, // TODO: choose an action type.
                "Video in short", // TODO: Define a title for the content shown.
                // TODO: If you have web page content that matches this app activity's content,
                // make sure this auto-generated web page URL is correct.
                // Otherwise, set the URL to null.
                Uri.parse("http://videoinshort.com"),
                // TODO: Make sure this auto-generated app deep link URI is correct.
                Uri.parse("android-app://com.vis.activities/http/videoinshort.com/video")
        );
        AppIndex.AppIndexApi.start(client, viewAction);*/
        AppIndex.AppIndexApi.start(client, getAction());
    }


    @Override
    protected void onStop() {
        // TODO Auto-generated method stub
        super.onStop();
        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
       /* Action viewAction = Action.newAction(
                Action.TYPE_VIEW, // TODO: choose an action type.
                "Video in short", // TODO: Define a title for the content shown.
                // TODO: If you have web page content that matches this app activity's content,
                // make sure this auto-generated web page URL is correct.
                // Otherwise, set the URL to null.
                Uri.parse("http://videoinshort.com"),
                // TODO: Make sure this auto-generated app deep link URI is correct.
                Uri.parse("android-app://com.vis.activities/http/videoinshort.com/video")
        );
        AppIndex.AppIndexApi.end(client, viewAction);*/
        AppIndex.AppIndexApi.end(client, getAction());
        GoogleAnalytics.getInstance(this).reportActivityStop(this);
        unregisterReceiver(broadcast_reciever);
        System.out.println("TRANSITION ONSTOP");
        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client.disconnect();
    }

    private void rateUs(String message) {

        final SharedPreferences prefs = getPreferences(this);
        final SharedPreferences.Editor editor = prefs.edit();
        Tracker t = ((Analytics) getApplication()).getDefaultTracker();
        t.enableAdvertisingIdCollection(true);
        // Build and send an Event.
        t.send(new HitBuilders.EventBuilder()
                .setCategory("Alert View")
                .setAction("Rate Us")
                .setLabel("Rate Us called")

                .build());


        AlertDialog dialog = new AlertDialog.Builder(this).create();
        t.enableAdvertisingIdCollection(true);
        dialog.setTitle("Rate Us!!");
        dialog.setMessage(message);
        dialog.setIcon(R.mipmap.ic_launcher);

        dialog.setButton(AlertDialog.BUTTON_POSITIVE, "Now", new DialogInterface.OnClickListener() {

            public void onClick(DialogInterface dialog, int id) {

                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + getApplicationContext().getPackageName())));
                editor.putBoolean(Constants.PREFERENCES_SHOW_ALARM, false).commit();
                editor.putBoolean(Constants.PREFERENCES_ALREADY_RATED, true).commit();


            }
        });

        dialog.setButton(AlertDialog.BUTTON_NEGATIVE, "Later", new DialogInterface.OnClickListener() {

            public void onClick(DialogInterface dialog, int id) {

                dialog.cancel();
                editor.putBoolean(Constants.PREFERENCES_SHOW_ALARM, false).commit();


            }
        });

        dialog.setButton(AlertDialog.BUTTON_NEUTRAL, "Never", new DialogInterface.OnClickListener() {

            public void onClick(DialogInterface dialog, int id) {

                dialog.cancel();
                editor.putBoolean(Constants.PREFERENCES_SHOW_ALARM, false).commit();
                editor.putBoolean(Constants.PREFERENCES_ALREADY_RATED, true).commit();


            }
        });

        dialog.show();

    }


    /**
     * Check the device to make sure it has the Google Play Services APK. If
     * it doesn't, display a dialog that allows users to download the APK from
     * the Google Play Store or enable it in the device's system settings.
     */
    private boolean checkPlayServices() {
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
                GooglePlayServicesUtil.getErrorDialog(resultCode, this,
                        Constants.PLAY_SERVICES_RESOLUTION_REQUEST).show();
            } else {
                Log.i(Constants.TAG, "This device is not supported.");
                finish();
            }
            return false;
        }
        return true;
    }

    /**
     * Stores the registration ID and the app versionCode in the application's
     * {@code SharedPreferences}.
     *
     * @param context application's context.
     * @param regId   registration ID
     */
    private void storeRegistrationId(Context context, String regId) {
        final SharedPreferences prefs = getPreferences(context);
        int appVersion = getAppVersion(context);
        Log.i(Constants.TAG, "Saving regId on app version " + appVersion);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(Constants.PROPERTY_REG_ID, regId);
        editor.putInt(Constants.PROPERTY_APP_VERSION, appVersion);
        editor.commit();
    }

    /**
     * Gets the current registration ID for application on GCM service, if there is one.
     * <p/>
     * If result is empty, the app needs to register.
     *
     * @return registration ID, or empty string if there is no existing
     * registration ID.
     */
    private String getRegistrationId(Context context) {
        final SharedPreferences prefs = getPreferences(context);
        String registrationId = prefs.getString(Constants.PROPERTY_REG_ID, "");
        if (registrationId.isEmpty()) {
            Log.i(Constants.TAG, "Registration not found.");
            return "";
        }
        // Check if app was updated; if so, it must clear the registration ID
        // since the existing regID is not guaranteed to work with the new
        // app version.
        int registeredVersion = prefs.getInt(Constants.PROPERTY_APP_VERSION, Integer.MIN_VALUE);
        int currentVersion = getAppVersion(context);
        if (registeredVersion != currentVersion) {
            Log.i(Constants.TAG, "App version changed.");
            return "";
        }
        return registrationId;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_logout) {
            Intent intent = new Intent(this, FacebookActivity.class);
            intent.putExtra(Constants.MENU_SETTINGS, true);
            startActivity(intent);
            finish();
            return true;
        } else if (id == R.id.action_feedback) {
            Intent intent = new Intent(this, FeedbackActivity.class);
            intent.putExtra(Constants.MENU_SETTINGS, true);
            startActivity(intent);
            return true;
        } else if (id == R.id.action_interest) {
            Intent intent = new Intent(this, InterestActivity.class);
            intent.putExtra(Constants.MENU_SETTINGS, true);
            startActivityForResult(intent, Constants.REQUEST_CODE_FOR_INTEREST);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * Registers the application with GCM servers asynchronously.
     * <p/>
     * Stores the registration ID and the app versionCode in the application's
     * shared preferences.
     */
    private void registerInBackground() {
        new AsyncTask<Void, Void, String>() {
            @Override
            protected String doInBackground(Void... params) {
                String msg = "";
                try {
                    if (gcm == null) {
                        gcm = GoogleCloudMessaging.getInstance(mContext);
                    }
                    regid = gcm.register(SENDER_ID);
                    msg = "Device registered, registration ID=" + regid;

                    // You should send the registration ID to your server over HTTP, so it
                    // can use GCM/HTTP or CCS to send messages to your app.
                    //  sendRegistrationIdToBackend();

                    // For this demo: we don't need to send it because the device will send
                    // upstream messages to a server that echo back the message using the
                    // 'from' address in the message.

                    // Persist the regID - no need to register again.
                    storeRegistrationId(mContext, regid);
                } catch (IOException ex) {
                    msg = "Error :" + ex.getMessage();
                    // If there is an error, don't just keep trying to register.
                    // Require the user to click a button again, or perform
                    // exponential back-off.
                }
                return msg;
            }

            @Override
            protected void onPostExecute(String msg) {
                // mDisplay.append(msg + "\n");
                callAllRequiredWebservices();
                checkYouTubeApi();
            }
        }.execute(null, null, null);
    }


    @Override
    protected void onResume() {
        System.out.println("TRANSITION RESUMED");
        super.onResume();
    }

    @Override
    protected void onPause() {
        System.out.println("TRANSITION PAUSED");
        super.onPause();
    }

    private void loadListView() {
        new CallWebservice().execute();
    }

    public class CallWebservice extends AsyncTask<Void, Void, List<VideoEntry>> {
        ProgressDialog dialog;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            dialog = new ProgressDialog(mContext);
            dialog.setCancelable(false);
            dialog.setMessage("Please wait..");
            dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            dialog.show();
        }

        @Override
        protected List<VideoEntry> doInBackground(Void... params) {

            Set<String> selectedInterest = prefs.getStringSet(Constants.PREFERENCES_SELECTED_INTERESTS, null);
            if (selectedInterest != null) {
                return getVideosListWithHashTags(selectedInterest);
            } else {
                return getVideosList();
            }

        }

        @Override
        protected void onPostExecute(List<VideoEntry> videoEntries) {
            super.onPostExecute(videoEntries);
            // VIDEO_LIST = videoEntries;
            /*adapter = new PageAdapter(mContext, videoEntries, fbProfile);
            recyclerView.setAdapter(adapter);
            recyclerView.requestLayout();*/
            //  setListViewHeightBasedOnItems(recyclerView);


            // use a linear layout manager
            if (videoEntries != null) {
                recyclerView.setHasFixedSize(true);
                PageAdapterForRecycler adapter = (PageAdapterForRecycler) recyclerView.getAdapter();
                adapter.setListEntries(videoEntries);
                adapter.notifyDataSetChanged();
                recyclerView.requestLayout();
            }
            dialog.cancel();
        }
    }

    private void hideViews() {
        mToolbar.animate().translationY(-mToolbar.getHeight()).setInterpolator(new AccelerateInterpolator(2));


    }

    private void showViews() {
        mToolbar.animate().translationY(0).setInterpolator(new DecelerateInterpolator(2));

    }

    public List<VideoEntry> getVideosList() {
        //Create request
        SoapObject request = new SoapObject(Constants.NAMESPACE, Constants.VIDEOS_METHOD_NAME);
        //Property which holds input parameters


        //Create envelope
        SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(
                SoapEnvelope.VER11);
        envelope.dotNet = true;
        //Set output SOAP object
        envelope.setOutputSoapObject(request);


        //Create HTTP call object
        HttpTransportSE androidHttpTransport = new HttpTransportSE(Constants.VIDEOS_URL);

        try {
            //Invole web service
            androidHttpTransport.call(Constants.VIDEOS_SOAP_ACTION, envelope);
            //Get the response
            //SoapObject response = (SoapObject) envelope.getResponse();
            //Assign it to fahren static variable

            List<VideoEntry> videosList = new ArrayList<VideoEntry>();

            SoapObject resultRequestSOAP = (SoapObject) envelope.bodyIn;
            SoapObject root = (SoapObject) resultRequestSOAP.getProperty("SentListOfVideoResult");
            int count = root.getPropertyCount();
            for (int i = 0; i < count; i++) {
                Object property = root.getProperty(i);
                if (property instanceof SoapObject) {
                    VideoEntry video = new VideoEntry();
                    SoapObject category_list = (SoapObject) property;
                    String postTitle = category_list.getProperty("PostTitle").toString();
                    String videoId = category_list.getProperty("VideoId").toString();
                    String hashTag1 = category_list.getProperty("Category1").toString();
                    String hashTag2 = category_list.getProperty("Category2").toString();
                    String hashTag3 = category_list.getProperty("Category3").toString();


                    video.setPostTitle(postTitle);
                    video.setVideoId(videoId);
                    video.setHashTag1(hashTag1);
                    video.setHashTag2(hashTag2);
                    video.setHashTag3(hashTag3);
                    videosList.add(video);
                }

            }
            mainList = videosList;
            List list;
            if (videosList.size() >= 33) {
                list = videosList.subList(0, 33);
            } else {
                list = videosList;
            }

            return list;


        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    public List<VideoEntry> getVideosListWithHashTags(Set<String> interests) {
        //Create request
        SoapObject request = new SoapObject(Constants.NAMESPACE, Constants.MULTIPLE_HASHTAGS_VIDEOS_METHOD_NAME);
        //Property which holds input parameters

        PropertyInfo userId = new PropertyInfo();
        //Set Name
        userId.setName("UserId");
        //Set Value
        userId.setValue(fbProfile.getFbUserId());
        //Set dataType
        userId.setType(String.class);
        //Add the property to request object
        String hashtags = "";
        for (String interest : interests) {
            hashtags += interest + ",";
        }
        if (hashtags.length() > 0) {
            hashtags = hashtags.substring(0, hashtags.length() - 1);
        } else {
            hashtags = "";
        }
        //Property which holds input parameters
        PropertyInfo hashtag = new PropertyInfo();
        //Set Name
        hashtag.setName("hashTag");
        //Set Value
        hashtag.setValue(hashtags);
        //Set dataType
        hashtag.setType(String.class);

        request.addProperty(hashtag);
        request.addProperty(userId);
        //Create envelope
        SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(
                SoapEnvelope.VER11);
        envelope.dotNet = true;
        //Set output SOAP object
        envelope.setOutputSoapObject(request);


        //Create HTTP call object
        HttpTransportSE androidHttpTransport = new HttpTransportSE(Constants.MULTIPLE_HASHTAGS_VIDEOS_URL);

        try {
            //Invole web service
            androidHttpTransport.call(Constants.MULTIPLE_HASHTAGS_VIDEOS_SOAP_ACTION, envelope);
            //Get the response
            //SoapObject response = (SoapObject) envelope.getResponse();
            //Assign it to fahren static variable

            List<VideoEntry> videosList = new ArrayList<VideoEntry>();

            SoapObject resultRequestSOAP = (SoapObject) envelope.bodyIn;
            SoapObject root = (SoapObject) resultRequestSOAP.getProperty("videoListWithMulipleHashTagResult");
            int count = root.getPropertyCount();
            for (int i = 0; i < count; i++) {
                Object property = root.getProperty(i);
                if (property instanceof SoapObject) {
                    VideoEntry video = new VideoEntry();
                    SoapObject category_list = (SoapObject) property;
                    String postTitle = category_list.getProperty("PostTitle").toString();
                    String videoId = category_list.getProperty("VideoId").toString();
                    String hashTag1 = category_list.getProperty("Category1").toString();
                    String hashTag2 = category_list.getProperty("Category2").toString();
                    String hashTag3 = category_list.getProperty("Category3").toString();


                    video.setPostTitle(postTitle);
                    video.setVideoId(videoId);
                    video.setHashTag1(hashTag1);
                    video.setHashTag2(hashTag2);
                    video.setHashTag3(hashTag3);
                    videosList.add(video);
                }

            }
            mainList = videosList;
            List list;
            if (videosList.size() >= 33) {
                list = videosList.subList(0, 33);
            } else {
                list = videosList;
            }

            return list;


        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    private void checkForAppUpdateApp(String appVer) {
        new AsyncTask<String, Void, String>() {
            @Override
            protected String doInBackground(String... params) {
                SoapObject request = new SoapObject(Constants.NAMESPACE, Constants.VERSION_METHOD_NAME);

                PropertyInfo appVersion = new PropertyInfo();
                appVersion.setName("appVersion");
                appVersion.setValue(params[0]);
                appVersion.setType(String.class);

                request.addProperty(appVersion);

                SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(
                        SoapEnvelope.VER11);
                envelope.dotNet = true;
                envelope.setOutputSoapObject(request);
                HttpTransportSE androidHttpTransport = new HttpTransportSE(Constants.VERSION_URL);

                try {
                    //Invole web service
                    androidHttpTransport.call(Constants.VERSION_SOAP_ACTION, envelope);
                    //Get the response
                    SoapPrimitive response = (SoapPrimitive) envelope.getResponse();
                    //Assign it to fahren static variable
                    String responseFromService = response.toString();

                    System.out.println("Response For Get App version " + responseFromService);
                    return responseFromService;
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return null;
            }

            @Override
            protected void onPostExecute(String result) {
                if (result != null) {
                    if (result.equals("1")) {
                        showUpdateMessage("Update your App!!");
                    }
                }
            }
        }.execute(appVer);

    }


    private void showUpdateMessage(String message) {

        final SharedPreferences prefs = getPreferences(mContext);
        Tracker t = ((Analytics) mContext.getApplicationContext()).getDefaultTracker();
        t.enableAdvertisingIdCollection(true);
        // Build and send an Event.
        t.send(new HitBuilders.EventBuilder()
                .setCategory("Alert View")
                .setAction("Update Available")
                .setLabel("Update Available called")
                .build());

        AlertDialog.Builder dialog = new AlertDialog.Builder(MainActivity.this);
        t.enableAdvertisingIdCollection(true);
        dialog.setTitle("Update Available!!")
                .setIcon(R.mipmap.ic_launcher)
                .setMessage(message)
                .setNegativeButton("Later", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialoginterface, int i) {
                        dialoginterface.cancel();
                    }
                })
                .setPositiveButton("Now", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialoginterface, int i) {
                        mContext.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + mContext.getApplicationContext().getPackageName())));
                    }
                }).show();

    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR1)
    private void showFullScreenVideo(String videoId) {
        String networkConstant = utility.connectedNetwork();
        String userId = null;
        if (fbProfile != null) {
            userId = fbProfile.getFbUserId();
        }
       /* VideoViewBean videoViewBean = new VideoViewBean();
        videoViewBean.setVideoId(videoId);
        videoViewBean.setUserId(userId);
        videoViewBean.setDate(new Date().toString());
        videoViewBean.setNetworkType(networkConstant);
        new WebServiceUtility(mContext, Constants.VIDEO_VIEW, videoViewBean);*/
        Intent intent = null;

        if (networkConstant != null && networkConstant.equals(Constants.WIFI)) {
            intent = new Intent(mContext, ShowVideoActivity.class);
        } else {
            intent = new Intent(mContext, ShowVideoInIFrameActivity.class);
        }

        intent.putExtra("VIDEO_ID", videoId);
        if (mContext instanceof MainActivity) {
            ((MainActivity) mContext).startActivityForResult(intent, 10);
        } else {
            ((HashTagActivity) mContext).startActivityForResult(intent, 10);
        }

    }
}
