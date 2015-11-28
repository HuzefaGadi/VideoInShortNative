/*
 * Copyright (C) 2013 The Android Open Source Project
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

package com.vis;

import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.google.gson.JsonSyntaxException;
import com.vis.Analytics.TrackerName;
import com.vis.activities.MyActivity;
import com.vis.beans.NotificationMessage;
import com.vis.utilities.Constants;
import com.vis.utilities.Utility;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.gson.Gson;
import com.vis.utilities.WebServiceUtility;

/**
 * This {@code IntentService} does the actual handling of the GCM message.
 * {@code GcmBroadcastReceiver} (a {@code WakefulBroadcastReceiver}) holds a
 * partial wake lock for this service while the service does its work. When the
 * service is finished, it calls {@code completeWakefulIntent()} to release the
 * wake lock.
 */
public class GcmIntentService extends IntentService {
	public static final int NOTIFICATION_ID = 1;
	private NotificationManager mNotificationManager;
	NotificationCompat.Builder builder;
	//private final String NAMESPACE = "http://tempuri.org/";
	/*private final String REGID_URL = "http://m.buzzonn.com/BuzzonFBList.asmx";
	private final String REGID_SOAP_ACTION = "http://tempuri.org/insertRegId";
	private final String REGID_METHOD_NAME = "insertRegId";*/

	/*private final String ACK_URL = "http://service.videoinshort.com/savefbuserdata.asmx";
	private final String ACK_SOAP_ACTION = "http://tempuri.org/SendClickReceiveNotiFication";
	private final String ACK_METHOD_NAME = "SendClickReceiveNotiFication";*/

	String responseFromService = null;


	public GcmIntentService() {
		super("GcmIntentService");
	}
	public static final String TAG = "GCM Demo";
	Utility utility;
	@Override
	protected void onHandleIntent(Intent intent) {
		Bundle extras = intent.getExtras();
		try {
			GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(this);
			utility = new Utility();
			// The getMessageType() intent parameter must be the intent you received
			// in your BroadcastReceiver.
			String messageType = gcm.getMessageType(intent);

			if (!extras.isEmpty()) {  // has effect of unparcelling Bundle
                /*
                 * Filter messages based on message type. Since it is likely that GCM will be
                 * extended in the future with new message types, just ignore any message types you're
                 * not interested in, or that you don't recognize.
                 */
                if (GoogleCloudMessaging.MESSAGE_TYPE_SEND_ERROR.equals(messageType)) {
                    sendNotification("Send error: " + extras.toString());
                } else if (GoogleCloudMessaging.MESSAGE_TYPE_DELETED.equals(messageType)) {
                    sendNotification("Deleted messages on server: " + extras.toString());
                    // If it's a regular GCM message, do some work.
                } else if (GoogleCloudMessaging.MESSAGE_TYPE_MESSAGE.equals(messageType)) {
                    // This loop represents the service doing some work.
                    /*for (int i = 0; i < 5; i++) {
                        Log.i(TAG, "Working... " + (i + 1)
                                + "/5 @ " + SystemClock.elapsedRealtime());
                        try {
                            Thread.sleep(5000);
                        } catch (InterruptedException e) {
                        }
                    }*/
                    Log.i(TAG, "Completed work @ " + SystemClock.elapsedRealtime());
                    // Post notification of received message.
                   	String notification = extras.getString("message");
                    if(utility.checkInternetConnectivity(this))
                    {
						new WebServiceUtility(getApplicationContext(),Constants.RECIEVE_INFO_TASK,notification);
                    }

                    sendNotification(notification);
                    Log.i(TAG, "Received: " + extras.toString());
                }
            }
		} catch (JsonSyntaxException e) {
			e.printStackTrace();
		}
		// Release the wake lock provided by the WakefulBroadcastReceiver.
		GcmBroadcastReceiver.completeWakefulIntent(intent);
	}

	// Put the message into a notification and post it.
	// This is just one simple example of what you might choose to do with
	// a GCM message.
	private void sendNotification(String notificationMessage) {
		try {
			mNotificationManager = (NotificationManager)
                    this.getSystemService(Context.NOTIFICATION_SERVICE);
			Gson gson = new Gson();
			NotificationMessage notification = gson.fromJson(notificationMessage, NotificationMessage.class);
			Uri alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
			Intent intent = new Intent(this, MyActivity.class);
			intent.putExtra("NOTIFICATION", notificationMessage);
			intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
			intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

			PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
                    intent, PendingIntent.FLAG_UPDATE_CURRENT);

			NotificationCompat.Builder mBuilder =
                    new NotificationCompat.Builder(this)
            .setSmallIcon(R.mipmap.ic_launcher)
							.setContentTitle("VideoEntry in Short")
							.setSound(alarmSound)
							.setAutoCancel(true)
							.setStyle(new NotificationCompat.BigTextStyle()
									.bigText(notification.getMessage()))
							.setContentText(notification.getMessage());

			mBuilder.setContentIntent(contentIntent);
			mNotificationManager.notify(NOTIFICATION_ID, mBuilder.build());
			Tracker t = ((Analytics)getApplication()).getTracker(
                    TrackerName.APP_TRACKER);
			// Build and send an Event.
			t.send(new HitBuilders.EventBuilder()
            .setCategory("GCM")
					.setAction("Message")
					.setLabel("Message recieved")
					.build());
		} catch (JsonSyntaxException e) {
			e.printStackTrace();
		}
	}


}
