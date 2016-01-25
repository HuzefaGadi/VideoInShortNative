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
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;
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
import com.vis.Analytics;
import com.vis.R;
import com.vis.adapters.PageAdapterForRecycler;
import com.vis.beans.AppActive;
import com.vis.beans.FbProfile;
import com.vis.beans.HashTagBean;
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
public class HashTagActivity extends AppCompatActivity {


    private static final int RECOVERY_DIALOG_REQUEST = 1;
    private RecyclerView recyclerView;

    RelativeLayout noInternetMessage, listViewContainer;
    Context mContext;

    Button refreshButton;
    Utility utility;
    FbProfile fbProfile;

    SharedPreferences prefs;
    boolean isHashTagFollowing = false;
    PageAdapterForRecycler adapter;
    private LinearLayoutManager mLayoutManager;
    private Toolbar mToolbar;
    private List<VideoEntry> mainList;
    String hashTag;
    ProgressDialog progressDialog;
    Button followUnFollow;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FacebookSdk.sdkInitialize(getApplicationContext());
        setContentView(R.layout.activity_hash_tag);

        utility = new Utility(this);
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Please wait..");
        progressDialog.setCancelable(false);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
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
        } else {
            String responseFromDb = prefs.getString(Constants.FB_USER_INFO, null);
            if (responseFromDb != null && !responseFromDb.isEmpty()) {
                fbProfile = new Gson().fromJson(responseFromDb, FbProfile.class);
            }
        }

        hashTag = getIntent().getStringExtra(Constants.HASHTAG);
        TextView mTitle = (TextView) mToolbar.findViewById(R.id.hashtag);
        mTitle.setText(hashTag);
        Button back = (Button) mToolbar.findViewById(R.id.button_back);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        followUnFollow = (Button) mToolbar.findViewById(R.id.button_followandunfollow);
        followUnFollow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                progressDialog.show();
                HashTagBean hash = new HashTagBean();
                hash.setUserId(fbProfile.getFbUserId());

                hash.setHashTag(hashTag);
                hash.setProgressDialog(progressDialog);
                String text = followUnFollow.getText().toString();
                if (text.equalsIgnoreCase("FOLLOW")) {
                    hash.setFlag("1");
                    followUnFollow.setText("UNFOLLOW");
                    new WebServiceUtility(HashTagActivity.this, Constants.FOLLOW_UNFOLLOW, hash);
                } else {
                    hash.setFlag("0");
                    followUnFollow.setText("FOLLOW");
                    new WebServiceUtility(HashTagActivity.this, Constants.FOLLOW_UNFOLLOW, hash);
                }


            }
        });


        if (checkPlayServices()) {

                checkYouTubeApi();

        } else {
            Log.i(Constants.TAG, "No valid Google Play Services APK found.");
        }


        refreshButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onRefresh();
            }
        });
        if (utility.checkInternetConnectivity()) {
            listViewContainer.setVisibility(View.VISIBLE);
            noInternetMessage.setVisibility(View.GONE);
            loadListView();
            // mainWebView.loadUrl(Constants.url);
        } else {
            listViewContainer.setVisibility(View.GONE);
            noInternetMessage.setVisibility(View.VISIBLE);
        }

        mLayoutManager = new LinearLayoutManager(HashTagActivity.this);
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
                if (interimCount < 103) {

                    if (mainList.size() >= 103) {
                        adapter.setListEntries(mainList.subList(0, adapter.getItemCount() + 33));
                        adapter.notifyDataSetChanged();
                        recyclerView.requestLayout();
                    }

                }


            }
        });
        //  new WebServiceUtility(this,Constants.GET_VIDEOS,null);
    }


    private void initToolbar() {
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        mToolbar.setTitle("");
       /* TextView title = (TextView) mToolbar.findViewById(R.id.toolbar_title);
        Typeface custom_font = Typeface.createFromAsset(getAssets(), "Calibri.ttf");
        title.setTypeface(custom_font);*/
        setSupportActionBar(mToolbar);

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

            recyclerView.requestLayout();

        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);


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
        GoogleAnalytics.getInstance(this).reportActivityStart(this);
    }


    @Override
    protected void onStop() {
        // TODO Auto-generated method stub
        super.onStop();
        GoogleAnalytics.getInstance(this).reportActivityStop(this);

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
            try
            {
                recyclerView.setHasFixedSize(true);
                if (videoEntries != null) {
                    PageAdapterForRecycler adapter = (PageAdapterForRecycler) recyclerView.getAdapter();
                    adapter.setListEntries(videoEntries);
                    adapter.notifyDataSetChanged();
                    recyclerView.requestLayout();
                    if (isHashTagFollowing) {
                        followUnFollow.setText("UNFOLLOW");
                    } else {
                        followUnFollow.setText("FOLLOW");
                    }
                }


            }catch(Exception e)
            {
                e.printStackTrace();
            }

            try{
                if (dialog != null && dialog.isShowing()) {
                    dialog.cancel();
                }
            }catch(Exception e)
            {
                e.printStackTrace();
            }

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
        SoapObject request = new SoapObject(Constants.NAMESPACE, Constants.HASHTAG_VIDEOS_METHOD_NAME);
        //Property which holds input parameters


        //Create envelope
        SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(
                SoapEnvelope.VER11);
        envelope.dotNet = true;
        //Set output SOAP object

        PropertyInfo hashTagProperty = new PropertyInfo();
        hashTagProperty.setName("hashTag");
        hashTagProperty.setValue(hashTag);
        hashTagProperty.setType(String.class);

        PropertyInfo userId = new PropertyInfo();
        userId.setName("UserId");
        userId.setValue(fbProfile.getFbUserId());
        userId.setType(String.class);


        request.addProperty(hashTagProperty);
        request.addProperty(userId);
        envelope.setOutputSoapObject(request);


        //Create HTTP call object
        HttpTransportSE androidHttpTransport = new HttpTransportSE(Constants.HASHTAG_VIDEOS_URL);

        try {
            //Invole web service
            androidHttpTransport.call(Constants.HASHTAG_VIDEOS_SOAP_ACTION, envelope);
            //Get the response
            //SoapObject response = (SoapObject) envelope.getResponse();
            //Assign it to fahren static variable

            List<VideoEntry> videosList = new ArrayList<VideoEntry>();
            SoapObject resultRequestSOAP = (SoapObject) envelope.bodyIn;
            String following = resultRequestSOAP.getPropertyAsString("tagstatus");
            isHashTagFollowing = following.equalsIgnoreCase("1") ? true : false;
            SoapObject root = (SoapObject) resultRequestSOAP.getProperty("SendVideoListByHashTagResult");
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
            if (mainList.size() >= 33) {
                list = videosList.subList(0, 33);
            } else {
                list = videosList.subList(0, mainList.size());
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

        Tracker t = ((Analytics) mContext.getApplicationContext()).getDefaultTracker();
        t.enableAdvertisingIdCollection(true);
        // Build and send an Event.
        t.send(new HitBuilders.EventBuilder()
                .setCategory("Alert View")
                .setAction("Rate Us")
                .setLabel("Rate Us called")
                .build());

        AlertDialog.Builder dialog = new AlertDialog.Builder(HashTagActivity.this);
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


}
