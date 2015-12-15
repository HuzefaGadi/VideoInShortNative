package com.vis;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.HashMap;

import android.app.Application;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.view.View;
import android.widget.Toast;

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

		writeIntoFile(e.getMessage());
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
}
