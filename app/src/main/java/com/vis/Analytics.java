package com.vis;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;

import android.app.Application;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;

import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.Tracker;

public class Analytics extends Application {


	private Tracker mTracker;

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
		// Setup handler for uncaught exceptions.
		Thread.setDefaultUncaughtExceptionHandler (new Thread.UncaughtExceptionHandler()
		{
			@Override
			public void uncaughtException (Thread thread, Throwable e)
			{
				handleUncaughtException (thread, e);
			}
		});

		super.onCreate();
	}

	public void handleUncaughtException (Thread thread, Throwable e)
	{
		e.printStackTrace();
		sendLogFile();
	}

	private String extractLogToFile()
	{
		PackageManager manager = this.getPackageManager();
		PackageInfo info = null;
		try {
			info = manager.getPackageInfo (this.getPackageName(), 0);
		} catch (PackageManager.NameNotFoundException e2) {
		}
		String model = Build.MODEL;
		if (!model.startsWith(Build.MANUFACTURER))
			model = Build.MANUFACTURER + " " + model;

		// Make file name - file must be saved to external storage or it wont be readable by
		// the email app.
		String path = Environment.getExternalStorageDirectory() + "/" + "MyApp/";
		String fullName = path + "Error.txt";

		// Extract to file.
		File file = new File (fullName);
		InputStreamReader reader = null;
		FileWriter writer = null;
		try
		{
			// For Android 4.0 and earlier, you will get all app's log output, so filter it to
			// mostly limit it to your app's output.  In later versions, the filtering isn't needed.
			String cmd = (Build.VERSION.SDK_INT <= Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1) ?
					"logcat -d -v time MyApp:v dalvikvm:v System.err:v *:s" :
					"logcat -d -v time";

			// get input stream
			Process process = Runtime.getRuntime().exec(cmd);
			reader = new InputStreamReader (process.getInputStream());

			// write output stream
			writer = new FileWriter (file);
			writer.write ("Android version: " +  Build.VERSION.SDK_INT + "\n");
			writer.write ("Device: " + model + "\n");
			writer.write ("App version: " + (info == null ? "(null)" : info.versionCode) + "\n");

			char[] buffer = new char[10000];
			do
			{
				int n = reader.read (buffer, 0, buffer.length);
				if (n == -1)
					break;
				writer.write (buffer, 0, n);
			} while (true);

			reader.close();
			writer.close();
		}
		catch (IOException e)
		{
			if (writer != null)
				try {
					writer.close();
				} catch (IOException e1) {
				}
			if (reader != null)
				try {
					reader.close();
				} catch (IOException e1) {
				}

			// You might want to write a failure message to the log here.
			return null;
		}

		return fullName;
	}

	private void sendLogFile ()
	{
		String fullName = extractLogToFile();
		if (fullName == null)
			return;

		Intent intent = new Intent (Intent.ACTION_SEND);
		intent.setType ("plain/text");
		intent.putExtra (Intent.EXTRA_EMAIL, new String[] {"learndroid53@gmail.com"});
		intent.putExtra (Intent.EXTRA_SUBJECT, "MyApp log file");
		intent.putExtra (Intent.EXTRA_STREAM, Uri.parse("file://" + fullName));
		intent.putExtra (Intent.EXTRA_TEXT, "Log file attached."); // do this so some email clients don't complain about empty body.
		startActivity (intent);
	}
}
