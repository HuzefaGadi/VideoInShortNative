package com.vis;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.HashMap;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.support.multidex.MultiDex;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.Tracker;
import com.google.gson.Gson;
import com.vis.beans.ErrorLog;
import com.vis.beans.FbProfile;
import com.vis.utilities.Constants;
import com.vis.utilities.WebServiceUtility;



/*@ReportsCrashes(mailTo = "learndroid53@gmail.com",
		customReportContent = { ReportField.APP_VERSION_CODE, ReportField.APP_VERSION_NAME, ReportField.ANDROID_VERSION, ReportField.PHONE_MODEL, ReportField.CUSTOM_DATA, ReportField.STACK_TRACE, ReportField.LOGCAT },
		mode = ReportingInteractionMode.TOAST,
		resToastText = R.string.crash_toast_text)*/

public class Analytics extends Application {


	private Tracker mTracker;
	FbProfile fbProfile;
	SharedPreferences prefs;

	/**
	 * Gets the default {@link Tracker} for this {@link Application}.
	 * @return tracker
	 */
	synchronized public Tracker getDefaultTracker() {
		if (mTracker == null) {
			GoogleAnalytics analytics = GoogleAnalytics.getInstance(this);
			// To enable debug logging use: adb shell setprop log.tag.GAv4 DEBUG
			mTracker = analytics.newTracker(R.xml.global_tracker);
		}
		return mTracker;
	}

	@Override
	public void onCreate ()
	{
		super.onCreate();
		prefs = getSharedPreferences(Constants.PREFERENCES_NAME,MODE_PRIVATE);
		// Setup handler for uncaught exceptions.
		Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
			@Override
			public void uncaughtException(Thread thread, Throwable e) {
				handleUncaughtException(thread, e);
			}
		});


		//ACRA.init(this);

	}

	public void handleUncaughtException (Thread thread, Throwable e)
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
		System.out.print("Caught Exception " + e.getStackTrace());
		if(fbProfile!=null)
		{
			userId = fbProfile.getFbUserId();
		}
		String androidVersion = android.os.Build.VERSION.RELEASE;
		ErrorLog errorLog = new ErrorLog();
		errorLog.setLogFile(convertStackTraceToString(e));
		errorLog.setUserId(userId);
		errorLog.setVersion(appVersion);
		errorLog.setOsVersion(androidVersion);
		errorLog.setDeviceModel(getDeviceName());
		new WebServiceUtility(this,Constants.LOG_ERROR,errorLog);

		//writeIntoFile(e.getMessage());
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


	private void sendLogFile (String error)
	{


		Intent email = new Intent(Intent.ACTION_SEND);
		email.putExtra(Intent.EXTRA_EMAIL, new String[]{"learndroid53@gmail.com"});
		email.putExtra(Intent.EXTRA_SUBJECT, "subject");
		email.putExtra(Intent.EXTRA_TEXT, error);
		email.setType("message/rfc822");
		startActivity(Intent.createChooser(email, "Choose an Email client :"));
	/*


		Intent intent = new Intent (Intent.ACTION_SEND);
		intent.setType("text/plain");
		intent.putExtra (Intent.EXTRA_EMAIL, new String[] {"learndroid53@gmail.com"});
		intent.putExtra (Intent.EXTRA_SUBJECT, "Vint log file");
		intent.putExtra (Intent.EXTRA_TEXT, error); // do this so some email clients don't complain about empty body.
		startActivity (intent);*/
	}

	public void writeIntoFile(String error) {
		// add-write text into file
		try {
			FileOutputStream fileout=openFileOutput("mytextfile.txt", MODE_PRIVATE);
			OutputStreamWriter outputWriter=new OutputStreamWriter(fileout);
			outputWriter.write(error);
			outputWriter.close();

			//display file saved message
			Toast.makeText(getBaseContext(), "File saved successfully!",
					Toast.LENGTH_SHORT).show();

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static String convertStackTraceToString(Throwable exception) {
		StringWriter sw = new StringWriter();
		PrintWriter pw = new PrintWriter(sw);
		exception.printStackTrace(pw);
		return sw.toString();
	}
}
