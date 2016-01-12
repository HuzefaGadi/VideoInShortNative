package com.vis.activities;

import android.app.Activity;
import android.content.SharedPreferences;
import android.content.SyncStatusObserver;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.vis.Analytics;
import com.vis.R;
import com.vis.beans.AppActive;
import com.vis.beans.ErrorLog;
import com.vis.beans.FbProfile;
import com.vis.beans.NotificationMessage;
import com.vis.beans.Registration;
import com.vis.beans.VideoViewBean;
import com.vis.utilities.Constants;
import com.vis.utilities.WebServiceUtility;

import org.ksoap2.SoapEnvelope;
import org.ksoap2.serialization.PropertyInfo;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapPrimitive;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpTransportSE;

import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * Created by huzefaasger on 12-01-2016.
 */
public class SendLog extends Activity
{
    FbProfile fbProfile;
    SharedPreferences prefs;
    String errorStackTrace;
    Button button;
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE); // make a dialog without a titlebar
        setFinishOnTouchOutside(false); // prevent users from dismissing the dialog by tapping outside
        setContentView(R.layout.send_log);
        System.out.println("Oncreate Called...");
        prefs = getSharedPreferences(Constants.PREFERENCES_NAME, MODE_PRIVATE);
        errorStackTrace = getIntent().getStringExtra("ERROR");
        System.out.println(errorStackTrace);
        button = (Button) findViewById(R.id.button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendLogFile();
                return;
            }
        });
    }



    private void sendLogFile ()
    {
        String appVersion = null;
        String userId =null;
        PackageInfo pInfo = null;
        try {
            pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
            appVersion = pInfo.versionName;
        } catch (PackageManager.NameNotFoundException e1) {
            e1.printStackTrace();
        }


        String fbUser = prefs.getString(Constants.FB_USER_INFO,null);
        if(fbUser!=null)
        {
            fbProfile = new Gson().fromJson(fbUser,FbProfile.class);
        }

        if(fbProfile!=null)
        {
            userId = fbProfile.getFbUserId();
        }
        String androidVersion = android.os.Build.VERSION.RELEASE;
        ErrorLog errorLog = new ErrorLog();
        errorLog.setLogFile(errorStackTrace);
        errorLog.setUserId(userId);
        errorLog.setVersion(appVersion);
        errorLog.setOsVersion(androidVersion);
        errorLog.setDeviceModel(getDeviceName());
        new AsyncCallWS(errorLog).execute();
        return;

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
    public void sendErrorLog(ErrorLog errorLog) {

        System.out.println("SEND ERROR LOG");
        SoapObject request = new SoapObject(Constants.NAMESPACE, Constants.ERROR_LOG_METHOD_NAME);

        PropertyInfo userId = new PropertyInfo();
        userId.setName("UserId");
        userId.setValue(errorLog.getUserId());
        userId.setType(String.class);

        PropertyInfo logFile = new PropertyInfo();
        logFile.setName("LogFile");
        logFile.setValue(errorLog.getLogFile());
        logFile.setType(String.class);

        PropertyInfo version = new PropertyInfo();
        version.setName("Version");
        version.setValue(errorLog.getVersion());
        version.setType(String.class);

        PropertyInfo osVersion = new PropertyInfo();
        osVersion.setName("OsVersion");
        osVersion.setValue(errorLog.getOsVersion());
        osVersion.setType(String.class);

        PropertyInfo deviceModel = new PropertyInfo();
        deviceModel.setName("deviceModel");
        deviceModel.setValue(errorLog.getDeviceModel());
        deviceModel.setType(String.class);


        //Add the property to request object
        request.addProperty(userId);
        request.addProperty(logFile);
        request.addProperty(version);
        request.addProperty(osVersion);
        request.addProperty(deviceModel);

        //Create envelope
        SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(
                SoapEnvelope.VER11);
        envelope.dotNet = true;
        //Set output SOAP object
        envelope.setOutputSoapObject(request);


        //Create HTTP call object
        HttpTransportSE androidHttpTransport = new HttpTransportSE(Constants.ERROR_LOG_URL);

        try {
            //Invole web service
            androidHttpTransport.call(Constants.ERROR_LOG_ACTION, envelope);
            //Get the response
            SoapPrimitive response = (SoapPrimitive) envelope.getResponse();
            //Assign it to fahren static variable
            String responseFromService = response.toString();
            System.out.println("Response for Error Log " + responseFromService);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private class AsyncCallWS extends AsyncTask<Void, Void, Void> {

        ErrorLog errorLog;

        public AsyncCallWS(ErrorLog errorLog) {
            this.errorLog = errorLog;
        }

        @Override
        protected Void doInBackground(Void... params) {
            Log.i(Constants.TAG, "doInBackground");
            sendErrorLog(errorLog);
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            Log.i(Constants.TAG, "onPostExecute");
            quit();
        }

        @Override
        protected void onPreExecute() {
            Log.i(Constants.TAG, "onPreExecute");

        }



    }
    public void quit() {

        int pid = android.os.Process.myPid();
        android.os.Process.killProcess(pid);
        System.exit(1);
        finish();
    }
}
