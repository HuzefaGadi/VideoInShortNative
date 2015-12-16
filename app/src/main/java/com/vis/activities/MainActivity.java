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
import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.facebook.FacebookSdk;
import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.android.youtube.player.YouTubeApiServiceUtil;
import com.google.android.youtube.player.YouTubeInitializationResult;
import com.google.gson.Gson;
import com.vis.AlarmReceiver;
import com.vis.Analytics;
import com.vis.FacebookActivity;
import com.vis.R;
import com.vis.adapters.PageAdapter;
import com.vis.beans.FbProfile;
import com.vis.beans.Registration;
import com.vis.beans.VideoEntry;
import com.vis.utilities.Constants;
import com.vis.utilities.Utility;
import com.vis.utilities.WebServiceUtility;

import org.ksoap2.SoapEnvelope;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpTransportSE;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * A sample Activity showing how to manage multiple YouTubeThumbnailViews in an adapter for display
 * in a List. When the list items are clicked, the video is played by using a YouTubePlayerFragment.
 * <p/>
 * The demo supports custom fullscreen and transitioning between portrait and landscape without
 * rebuffering.
 */
@TargetApi(13)
public final class MainActivity extends AppCompatActivity {

    /**
     * The duration of the animation sliding up the video in portrait.
     */
    private static final int ANIMATION_DURATION_MILLIS = 300;
    /**
     * The padding between the video list and the video in landscape orientation.
     */
    private static final int LANDSCAPE_VIDEO_PADDING_DP = 5;

    /**
     * The request code when calling startActivityForResult to recover from an API service error.
     */
    private static final int RECOVERY_DIALOG_REQUEST = 1;

    private ListView listView;


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
    PageAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FacebookSdk.sdkInitialize(getApplicationContext());
        setContentView(R.layout.video_list_demo);
        toolbar = (Toolbar) findViewById(R.id.MyToolbar);
        noInternetMessage = (RelativeLayout) findViewById(R.id.no_internet_message);
        listViewContainer = (RelativeLayout) findViewById(R.id.list_view_container);
        refreshButton = (Button) findViewById(R.id.refreshButton);
        setSupportActionBar(toolbar);
        //getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        GoogleAnalytics.getInstance(this).reportActivityStart(this);
        collapsingToolbar =
                (CollapsingToolbarLayout) findViewById(R.id.collapse_toolbar);
        collapsingToolbar.setTitle("Vint");

        appBarLayout = (AppBarLayout) findViewById(R.id.MyAppbar);

        NestedScrollView scrollView = (NestedScrollView) findViewById(R.id.nested_scroll_view);
        scrollView.setFillViewport(true);

        mContext = this;
        prefs = getSharedPreferences(Constants.PREFERENCES_NAME, MODE_PRIVATE);
        listView = (ListView) findViewById(R.id.list_fragment);
// Check device for Play Services APK. If check succeeds, proceed with GCM registration.


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
                doFacebookThing();
                checkYouTubeApi();
            }
        } else {
            Log.i(Constants.TAG, "No valid Google Play Services APK found.");
        }


        if (prefs.getBoolean("ALARM_SET", false)) {

        } else {
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
        utility = new Utility();

        refreshButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onRefresh();
            }
        });
        if (utility.checkInternetConnectivity(mContext)) {
            listViewContainer.setVisibility(View.VISIBLE);
            noInternetMessage.setVisibility(View.GONE);
            loadListView();
            // mainWebView.loadUrl(Constants.url);
        } else {
            listViewContainer.setVisibility(View.GONE);
            noInternetMessage.setVisibility(View.VISIBLE);
        }

        //  new WebServiceUtility(this,Constants.GET_VIDEOS,null);
    }


    private void doFacebookThing() {

        {
            SharedPreferences pref = getPreferences(mContext);

            if (fbProfile == null) {
                String responseFromFb = pref.getString(Constants.FB_USER_INFO, null);
                if (responseFromFb != null) {
                    fbProfile = new Gson().fromJson(responseFromFb, FbProfile.class);
                    new WebServiceUtility(getApplicationContext(), Constants.SEND_FACEBOOK_DATA, fbProfile);
                }
            } else {
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
            new WebServiceUtility(getApplicationContext(), Constants.SEND_APP_ACTIVE_DATA, regId);
            new WebServiceUtility(getApplicationContext(), Constants.UPDATE_APP, getAppVersionName(getApplicationContext()));
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
        } else {
            super.onActivityResult(requestCode, resultCode, data);
            adapter.callbackManager.onActivityResult(requestCode, resultCode, data);
            listView.clearChoices();
            listView.requestLayout();

        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);


    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        processIntent(getIntent());
    }

    void processIntent(Intent intent) {
        String notification = intent.getStringExtra("NOTIFICATION");
        if (notification != null && !notification.isEmpty()) {
            String regId = getRegistrationId(mContext);
            new WebServiceUtility(this, Constants.CLICK_INFO_TASK, notification);
            new WebServiceUtility(getApplicationContext(), Constants.SEND_APP_ACTIVE_DATA, regId);
        }
        onRefresh();

    }

    public void onRefresh() {
        // TODO Auto-generated method stub
		/*
		new Handler().postDelayed(new Runnable() {
			@Override public void run() {
				swipeLayout.setRefreshing(false);
			}
		}, 5000);*/
        if (utility.checkInternetConnectivity(mContext)) {
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

        System.out.println("TRANSITION ONSTART");
        SharedPreferences pref = getPreferences(this);
        if (pref.getBoolean(Constants.PREFERENCES_SHOW_ALARM, false) && !pref.getBoolean(Constants.PREFERENCES_ALREADY_RATED, false)) {
            rateUs("You are awesome! If you feel the same about VideoInShort, please take a moment to rate it.");
        }
        GoogleAnalytics.getInstance(this).reportActivityStart(this);
        registerReceiver(broadcast_reciever, new IntentFilter("finish_activity"));
    }


    @Override
    protected void onStop() {
        // TODO Auto-generated method stub
        super.onStop();
        GoogleAnalytics.getInstance(this).reportActivityStop(this);
        unregisterReceiver(broadcast_reciever);
        System.out.println("TRANSITION ONSTOP");
    }

    private void rateUs(String message) {

        final SharedPreferences prefs = getPreferences(this);
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
		/*AlertDialog.Builder dialog = new AlertDialog.Builder(this);
		t.enableAdvertisingIdCollection(true);
		dialog.setTitle( "Rate Us!!" )
		.setIcon(R.drawable.ic_launcher)
		.setMessage(message)
		.setNegativeButton("Later", new DialogInterface.OnClickListener()
		{
			public void onClick(DialogInterface dialoginterface, int i)
			{
				dialoginterface.cancel();
				prefs.edit().putBoolean("SHOWALARM", false).commit();
			}})
			.setPositiveButton("Now", new DialogInterface.OnClickListener()
			{
				public void onClick(DialogInterface dialoginterface, int i)
				{
					startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + getApplicationContext().getPackageName())));
					prefs.edit().putBoolean("SHOWALARM", false).commit();
					prefs.edit().putBoolean("ALREADYRATED", true).commit();
				}
			}).show();*/


        dialog.setButton(AlertDialog.BUTTON_POSITIVE, "Now", new DialogInterface.OnClickListener() {

            public void onClick(DialogInterface dialog, int id) {

                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + getApplicationContext().getPackageName())));
                prefs.edit().putBoolean(Constants.PREFERENCES_SHOW_ALARM, false).commit();
                prefs.edit().putBoolean(Constants.PREFERENCES_ALREADY_RATED, true).commit();

            }
        });

        dialog.setButton(AlertDialog.BUTTON_NEGATIVE, "Later", new DialogInterface.OnClickListener() {

            public void onClick(DialogInterface dialog, int id) {

                dialog.cancel();
                prefs.edit().putBoolean(Constants.PREFERENCES_SHOW_ALARM, false).commit();

            }
        });

        dialog.setButton(AlertDialog.BUTTON_NEUTRAL, "Never", new DialogInterface.OnClickListener() {

            public void onClick(DialogInterface dialog, int id) {

                dialog.cancel();
                prefs.edit().putBoolean(Constants.PREFERENCES_SHOW_ALARM, false).commit();
                prefs.edit().putBoolean(Constants.PREFERENCES_ALREADY_RATED, true).commit();

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
        if (id == R.id.action_settings) {
            Intent intent = new Intent(this, FacebookActivity.class);
            intent.putExtra(Constants.MENU_SETTINGS, true);
            startActivity(intent);
            finish();
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
                doFacebookThing();
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
            return getVideosList();
        }

        @Override
        protected void onPostExecute(List<VideoEntry> videoEntries) {
            super.onPostExecute(videoEntries);
            // VIDEO_LIST = videoEntries;
            adapter = new PageAdapter(mContext, videoEntries, fbProfile);

            listView.setAdapter(adapter);
            listView.requestLayout();
            setListViewHeightBasedOnItems(listView);
            dialog.cancel();
        }
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

                    video.setPostTitle(postTitle);
                    video.setVideoId(videoId);
                    videosList.add(video);
                }

            }
            return videosList.subList(0, 20);

        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }


    public static boolean setListViewHeightBasedOnItems(ListView listView) {

        ListAdapter listAdapter = listView.getAdapter();
        if (listAdapter != null) {

            int numberOfItems = listAdapter.getCount();

            // Get total height of all items.
            int totalItemsHeight = 0;
            for (int itemPos = 0; itemPos < numberOfItems; itemPos++) {
                View item = listAdapter.getView(itemPos, null, listView);
                item.measure(0, 0);
                totalItemsHeight += item.getMeasuredHeight() + 20;
            }

            // Get total height of all item dividers.
            int totalDividersHeight = listView.getDividerHeight() *
                    (numberOfItems - 1);

            // Set list height.
            ViewGroup.LayoutParams params = listView.getLayoutParams();
            params.height = totalItemsHeight + totalDividersHeight;
            listView.setLayoutParams(params);
            listView.requestLayout();

            return true;

        } else {
            return false;
        }

    }


}
