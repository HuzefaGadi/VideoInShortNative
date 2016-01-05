package com.vis.utilities;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.vis.Analytics;
import com.vis.R;
import com.vis.activities.MainActivity;
import com.vis.beans.AppActive;
import com.vis.beans.Contact;
import com.vis.beans.FbProfile;
import com.vis.beans.Location;
import com.vis.beans.NotificationMessage;
import com.vis.beans.Registration;
import com.vis.beans.VideoEntry;
import com.vis.beans.VideoViewBean;

import org.ksoap2.SoapEnvelope;
import org.ksoap2.serialization.PropertyInfo;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapPrimitive;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpTransportSE;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by huzefaasger on 07-09-2015.
 */
public class WebServiceUtility {

    Context mContext;
    SharedPreferences preferences;
    List<Contact> listOfContacts;
    String appVersion;

    public WebServiceUtility(Context context, int action, Object object) {
        mContext = context;
        preferences = getPreferences(mContext);
        PackageInfo pInfo = null;
        try {
            pInfo = mContext.getPackageManager().getPackageInfo(mContext.getPackageName(), 0);
            appVersion = pInfo.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        new AsyncCallWS(action).execute(object);
    }

    private class AsyncCallWS extends AsyncTask<Object, Void, String> {

        int action;

        public AsyncCallWS(int action) {
            this.action = action;
        }

        @Override
        protected String doInBackground(Object... params) {
            Log.i(Constants.TAG, "doInBackground");
            SharedPreferences pref = getPreferences(mContext);

            if (action == Constants.SEND_FACEBOOK_DATA) {
                Tracker t = ((Analytics) mContext.getApplicationContext()).getDefaultTracker();
                // Build and send an Event.
                t.send(new HitBuilders.EventBuilder()
                        .setCategory("USERDATA")
                        .setAction("User data sent")
                        .setLabel("User data Upload")
                        .build());
                PackageInfo pInfo;
                String appVersion = null;
                try {
                    pInfo = mContext.getPackageManager().getPackageInfo(mContext.getPackageName(), 0);
                    appVersion = pInfo.versionName;
                } catch (PackageManager.NameNotFoundException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }

                insertFacebookNewUserData((FbProfile) params[0]);
            } else if (action == Constants.SEND_APP_ACTIVE_DATA) {
                Tracker t = ((Analytics) mContext.getApplicationContext()).getDefaultTracker();
                // Build and send an Event.
                t.send(new HitBuilders.EventBuilder()
                        .setCategory("App Active")
                        .setAction("App Opened")
                        .setLabel("App Opened By User")
                        .build());
                PackageInfo pInfo;
                String appVersion = null;
                try {
                    pInfo = mContext.getPackageManager().getPackageInfo(mContext.getPackageName(), 0);
                    appVersion = pInfo.versionName;
                } catch (PackageManager.NameNotFoundException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }

                insertAppActive((AppActive) params[0]);
            } else if (action == Constants.CLICK_INFO_TASK) {
                String json = (String) params[0];
                Gson gson = new Gson();
                try {
                    NotificationMessage notification = gson.fromJson(json, NotificationMessage.class);
                    sendAcknowledgementForClickStatus(notification);
                } catch (JsonSyntaxException e) {
                    e.printStackTrace();
                } catch (Exception e) {
                    e.printStackTrace();
                }


            } else if (action == Constants.RECIEVE_INFO_TASK) {
                String json = (String) params[0];
                Gson gson = new Gson();
                try {
                    NotificationMessage notification = gson.fromJson(json, NotificationMessage.class);
                    sendAcknowledgementForRecieveStatus(notification);
                } catch (JsonSyntaxException e) {
                    e.printStackTrace();
                } catch (Exception e) {
                    e.printStackTrace();
                }

            } else if (action == Constants.USER_INFO_TASK) {


                Tracker t = ((Analytics) mContext).getDefaultTracker();
                // Build and send an Event.
                t.send(new HitBuilders.EventBuilder()
                        .setCategory("GCM")
                        .setAction("Reg Id sent")
                        .setLabel("Reg Id upload")
                        .build());
                PackageInfo pInfo;

                Registration registration = (Registration) params[0];
                insertRegId(registration);
                    /*listOfContacts = new Phonebook(mContext).readContacts();

                    if (pref.getInt("CONTACTS", 0) < listOfContacts.size()) {
                        // Get tracker.
                        Tracker t2 = ((Analytics) mContext).getTracker(
                                Analytics.TrackerName.APP_TRACKER);
                        // Build and send an Event.
                        t2.send(new HitBuilders.EventBuilder()
                                .setCategory("Contacts")
                                .setAction("Contacts sent")
                                .setLabel("Contacts upload")
                                .build());
                        String response = sendContactDetails((String) params[0]);

                        if (response != null) {
                            if (response.equals("1")) {
                                pref.edit().putInt("CONTACTS", listOfContacts.size()).commit();
                            }

                        }
                    }*/

            } else if (action == Constants.VIDEO_VIEW) {
                callVideoViewed((VideoViewBean) params[0]);
            } else if (action == Constants.SHARE_DATA) {
                callShareVideo((VideoViewBean) params[0]);
            }


            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            Log.i(Constants.TAG, "onPostExecute");
        }

        @Override
        protected void onPreExecute() {
            Log.i(Constants.TAG, "onPreExecute");

        }

        @Override
        protected void onProgressUpdate(Void... values) {
            Log.i(Constants.TAG, "onProgressUpdate");
        }

    }

    private void callShareVideo(VideoViewBean videoView) {


        //Create request
        SoapObject request = new SoapObject(Constants.NAMESPACE, Constants.VIDEOS_SHARE_METHOD_NAME);
        //Property which holds input parameters
        PropertyInfo userId = new PropertyInfo();
        //Set Name
        userId.setName("UserId");
        //Set Value
        userId.setValue(videoView.getUserId());
        //Set dataType
        userId.setType(String.class);
        //Add the property to request object

        //Property which holds input parameters
        PropertyInfo videoId = new PropertyInfo();
        //Set Name
        videoId.setName("VideoId");
        //Set Value
        videoId.setValue(videoView.getVideoId());
        //Set dataType
        videoId.setType(String.class);
        //Add the property to request object


        //Property which holds input parameters
        PropertyInfo type = new PropertyInfo();
        //Set Name
        type.setName("Constant");
        //Set Value
        type.setValue(videoView.getType());
        //Set dataType
        type.setType(String.class);

        //Add the property to request object
        request.addProperty(userId);
        request.addProperty(videoId);
        request.addProperty(type);

        // System.out.println("Video Id "+videoView.getDate() +" "+videoView.getUserId()+" "+videoView.getVideoId());
        //Create envelope
        SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(
                SoapEnvelope.VER11);
        envelope.dotNet = true;
        //Set output SOAP object
        envelope.setOutputSoapObject(request);


        //Create HTTP call object

        HttpTransportSE androidHttpTransport = new HttpTransportSE(Constants.VIDEOS_SHARE_URL);

        try {
            //Invole web service
            androidHttpTransport.call(Constants.VIDEOS_SHARE_SOAP_ACTION, envelope);
            //Get the response
            SoapPrimitive response = (SoapPrimitive) envelope.getResponse();
            //Assign it to fahren static variable
            String responseFromService = response.toString();
            Log.d("vis", responseFromService);
            System.out.println("Response from Video share status" + responseFromService);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void callVideoViewed(VideoViewBean videoView) {


        //Create request
        SoapObject request = new SoapObject(Constants.NAMESPACE, Constants.VIDEOS_VIEW_METHOD_NAME);
        //Property which holds input parameters
        PropertyInfo userId = new PropertyInfo();
        //Set Name
        userId.setName("userId");
        //Set Value
        userId.setValue(videoView.getUserId());
        //Set dataType
        userId.setType(String.class);
        //Add the property to request object

        //Property which holds input parameters
        PropertyInfo videoId = new PropertyInfo();
        //Set Name
        videoId.setName("Videoid");
        //Set Value
        videoId.setValue(videoView.getVideoId());
        //Set dataType
        videoId.setType(String.class);
        //Add the property to request object


        //Property which holds input parameters
        PropertyInfo date = new PropertyInfo();
        //Set Name
        date.setName("date");
        //Set Value
        date.setValue(videoView.getDate());
        //Set dataType
        date.setType(String.class);

        //Add the property to request object
        request.addProperty(userId);
        request.addProperty(videoId);
        request.addProperty(date);

        // System.out.println("Video Id "+videoView.getDate() +" "+videoView.getUserId()+" "+videoView.getVideoId());
        //Create envelope
        SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(
                SoapEnvelope.VER11);
        envelope.dotNet = true;
        //Set output SOAP object
        envelope.setOutputSoapObject(request);


        //Create HTTP call object

        HttpTransportSE androidHttpTransport = new HttpTransportSE(Constants.VIDEOS_VIEW_URL);

        try {
            //Invole web service
            androidHttpTransport.call(Constants.VIDEOS_VIEW_SOAP_ACTION, envelope);
            //Get the response
            SoapPrimitive response = (SoapPrimitive) envelope.getResponse();
            //Assign it to fahren static variable
            String responseFromService = response.toString();
            Log.d("vis", responseFromService);
            System.out.println("Response from Video view status" + responseFromService);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private SharedPreferences getPreferences(Context context) {
        // This sample app persists the registration ID in shared preferences, but
        // how you store the regID in your app is up to you.
        return mContext.getSharedPreferences(Constants.PREFERENCES_NAME,
                Context.MODE_PRIVATE);
    }

    public void insertFacebookNewUserData(FbProfile fbProfile) {

        SoapObject request = new SoapObject(Constants.NAMESPACE, Constants.NEW_USER_METHOD_NAME);
        PropertyInfo firstName = new PropertyInfo();
        firstName.setName("FirstName");
        firstName.setValue(fbProfile.getFirstName());
        firstName.setType(String.class);

        PropertyInfo lastName = new PropertyInfo();
        lastName.setName("LastName");
        lastName.setValue(fbProfile.getLastName());
        lastName.setType(String.class);

        PropertyInfo email = new PropertyInfo();
        email.setName("Email");
        email.setValue(fbProfile.getEmail());
        email.setType(String.class);

        PropertyInfo cityName = new PropertyInfo();
        cityName.setName("CountryCity");
        cityName.setType(String.class);

        PropertyInfo countryName = new PropertyInfo();
        countryName.setName("CountryName");
        countryName.setType(String.class);

        Location location = fbProfile.getLocation();
        if (location != null) {
            if (location.getCountry() == null && location.getCity() == null) {
                cityName.setValue(location.getName());
                countryName.setValue("");
            } else {
                cityName.setValue(location.getCity());
                countryName.setValue(location.getCountry());
            }
        } else {
            cityName.setValue("");
            countryName.setValue("");
        }

        PropertyInfo profileImagePath = new PropertyInfo();
        profileImagePath.setName("ProfileImagePath");
        profileImagePath.setValue(fbProfile.getProfileImagePath());
        profileImagePath.setType(String.class);

        PropertyInfo fbUserId = new PropertyInfo();
        fbUserId.setName("FBUserId");
        fbUserId.setValue(fbProfile.getFbUserId());
        fbUserId.setType(String.class);

        PropertyInfo dob = new PropertyInfo();
        dob.setName("dob");
        dob.setValue(fbProfile.getDateOfBirth());
        dob.setType(String.class);

        PropertyInfo gender = new PropertyInfo();
        gender.setName("Gender");
        gender.setValue(fbProfile.getGender());
        gender.setType(String.class);

        PropertyInfo facebookProfileLink = new PropertyInfo();
        facebookProfileLink.setName("FacebookProfileLink");
        facebookProfileLink.setValue(fbProfile.getFbProfileLink());
        facebookProfileLink.setType(String.class);


        String registrationId = preferences.getString(Constants.PROPERTY_REG_ID, "");
        PropertyInfo mobRegId = new PropertyInfo();
        mobRegId.setName("MobRegId");
        mobRegId.setValue(registrationId);
        mobRegId.setType(String.class);

        PropertyInfo mobNumber = new PropertyInfo();
        mobNumber.setName("MobNumber");
        mobNumber.setValue(fbProfile.getMobileNumber());
        mobNumber.setType(String.class);


        request.addProperty(firstName);
        request.addProperty(lastName);
        request.addProperty(email);
        request.addProperty(cityName);
        request.addProperty(countryName);
        request.addProperty(profileImagePath);
        request.addProperty(fbUserId);
        request.addProperty(dob);
        request.addProperty(gender);
        request.addProperty(facebookProfileLink);
        request.addProperty(mobRegId);
        request.addProperty(mobNumber);

        SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(
                SoapEnvelope.VER11);
        envelope.dotNet = true;
        envelope.setOutputSoapObject(request);
        HttpTransportSE androidHttpTransport = new HttpTransportSE(Constants.NEW_USER_URL);

        try {
            //Invole web service
            androidHttpTransport.call(Constants.NEW_USER_SOAP_ACTION, envelope);
            //Get the response
            SoapPrimitive response = (SoapPrimitive) envelope.getResponse();
            //Assign it to fahren static variable
            String responseFromService = response.toString();
            System.out.println("Response for New Facebook User" + responseFromService);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void insertAppActive(AppActive appActive) {

        SoapObject request = new SoapObject(Constants.NAMESPACE, Constants.ACTIVE_METHOD_NAME);

        PropertyInfo mobileRegistrationId = new PropertyInfo();
        mobileRegistrationId.setName("mobileRegistrationId");
        mobileRegistrationId.setValue(appActive.getRegId());
        mobileRegistrationId.setType(String.class);

        PropertyInfo version = new PropertyInfo();
        version.setName("version");
        version.setValue(appVersion);
        version.setType(String.class);

        PropertyInfo networkConstant = new PropertyInfo();
        networkConstant.setName("_constant");
        networkConstant.setValue(appActive.getNetworkConstant());
        networkConstant.setType(String.class);




        request.addProperty(mobileRegistrationId);
        request.addProperty(version);
        request.addProperty(networkConstant);

        SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(
                SoapEnvelope.VER11);
        envelope.dotNet = true;
        envelope.setOutputSoapObject(request);
        HttpTransportSE androidHttpTransport = new HttpTransportSE(Constants.ACTIVE_URL);


        try {
            //Invole web service
            androidHttpTransport.call(Constants.ACTIVE_SOAP_ACTION, envelope);
            //Get the response
            SoapPrimitive response = (SoapPrimitive) envelope.getResponse();
            //Assign it to fahren static variable
            String responseFromService = response.toString();
            System.out.println("Response For Insert App Active" + responseFromService);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


   /* public void sendAcknowledgementForClickStatus(NotificationMessage
                                                          notification) {

        //Create request
        SoapObject request = new SoapObject(Constants.NAMESPACE, Constants.ACK_METHOD_NAME);
        //Property which holds input parameters
        PropertyInfo userId = new PropertyInfo();
        //Set Name
        userId.setName("UserId");
        //Set Value
        userId.setValue(notification.getUid());
        //Set dataType
        userId.setType(String.class);
        //Add the property to request object

        //Property which holds input parameters
        PropertyInfo notificationId = new PropertyInfo();
        //Set Name
        notificationId.setName("NotificationId");
        //Set Value
        notificationId.setValue(notification.getNotificationId());
        //Set dataType
        notificationId.setType(String.class);
        //Add the property to request object


        //Property which holds input parameters
        PropertyInfo clickStatus = new PropertyInfo();
        //Set Name
        clickStatus.setName("clickStatus");
        //Set Value
        clickStatus.setValue("1");
        //Set dataType
        clickStatus.setType(String.class);

        //Property which holds input parameters
        PropertyInfo recStatus = new PropertyInfo();
        //Set Name
        recStatus.setName("receiveStatus");
        //Set Value
        recStatus.setValue("1");
        //Set dataType
        recStatus.setType(String.class);


        //Add the property to request object
        request.addProperty(userId);
        request.addProperty(notificationId);
        request.addProperty(recStatus);
        request.addProperty(clickStatus);


        //Create envelope
        SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(
                SoapEnvelope.VER11);
        envelope.dotNet = true;
        //Set output SOAP object
        envelope.setOutputSoapObject(request);


        //Create HTTP call object

        HttpTransportSE androidHttpTransport = new HttpTransportSE(Constants.ACK_URL);

        try {
            //Invole web service
            androidHttpTransport.call(Constants.ACK_SOAP_ACTION, envelope);
            //Get the response
            SoapPrimitive response = (SoapPrimitive) envelope.getResponse();
            //Assign it to fahren static variable
            String responseFromService = response.toString();
            System.out.println("Response from CLICK status" + responseFromService);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }*/

    public void sendAcknowledgementForClickStatus(NotificationMessage
                                                          notification) {

        //Create request
        SoapObject request = new SoapObject(Constants.NAMESPACE, Constants.CLICK_ACK_METHOD_NAME);
        //Property which holds input parameters
        PropertyInfo userId = new PropertyInfo();
        //Set Name
        userId.setName("UserId");
        //Set Value
        userId.setValue(notification.getUid());
        //Set dataType
        userId.setType(String.class);
        //Add the property to request object

        //Property which holds input parameters
        PropertyInfo notificationId = new PropertyInfo();
        //Set Name
        notificationId.setName("NotificationId");
        //Set Value
        notificationId.setValue(notification.getNotificationId());
        //Set dataType
        notificationId.setType(String.class);
        //Add the property to request object


        //Property which holds input parameters
        PropertyInfo clickStatus = new PropertyInfo();
        //Set Name
        clickStatus.setName("clickStatus");
        //Set Value
        clickStatus.setValue("1");
        //Set dataType
        clickStatus.setType(String.class);

        //Add the property to request object
        request.addProperty(userId);
        request.addProperty(notificationId);
        request.addProperty(clickStatus);


        //Create envelope
        SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(
                SoapEnvelope.VER11);
        envelope.dotNet = true;
        //Set output SOAP object
        envelope.setOutputSoapObject(request);


        //Create HTTP call object

        HttpTransportSE androidHttpTransport = new HttpTransportSE(Constants.CLICK_ACK_URL);

        try {
            //Invole web service
            androidHttpTransport.call(Constants.CLICK_ACK_SOAP_ACTION, envelope);
            //Get the response
            SoapPrimitive response = (SoapPrimitive) envelope.getResponse();
            //Assign it to fahren static variable
            String responseFromService = response.toString();
            Log.d("vis", responseFromService);
            System.out.println("Response from CLICK status" + responseFromService);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void sendAcknowledgementForRecieveStatus(NotificationMessage
                                                            notification) {

        //Create request
        SoapObject request = new SoapObject(Constants.NAMESPACE, Constants.ACK_METHOD_NAME);
        //Property which holds input parameters
        PropertyInfo userId = new PropertyInfo();
        //Set Name
        userId.setName("UserId");
        //Set Value
        userId.setValue(notification.getUid());
        //Set dataType
        userId.setType(String.class);
        //Add the property to request object

        //Property which holds input parameters
        PropertyInfo notificationId = new PropertyInfo();
        //Set Name
        notificationId.setName("NotificationId");
        //Set Value
        notificationId.setValue(notification.getNotificationId());
        //Set dataType
        notificationId.setType(String.class);
        //Add the property to request object


        //Property which holds input parameters
        PropertyInfo clickStatus = new PropertyInfo();
        //Set Name
        clickStatus.setName("clickStatus");
        //Set Value
        clickStatus.setValue("0");
        //Set dataType
        clickStatus.setType(String.class);

        //Property which holds input parameters
        PropertyInfo recStatus = new PropertyInfo();
        //Set Name
        recStatus.setName("receiveStatus");
        //Set Value
        recStatus.setValue("1");
        //Set dataType
        recStatus.setType(String.class);


        //Add the property to request object
        request.addProperty(userId);
        request.addProperty(notificationId);
        request.addProperty(recStatus);
        request.addProperty(clickStatus);


        //Create envelope
        SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(
                SoapEnvelope.VER11);
        envelope.dotNet = true;
        //Set output SOAP object
        envelope.setOutputSoapObject(request);


        //Create HTTP call object

        HttpTransportSE androidHttpTransport = new HttpTransportSE(Constants.ACK_URL);

        try {
            //Invole web service
            androidHttpTransport.call(Constants.ACK_SOAP_ACTION, envelope);
            //Get the response
            SoapPrimitive response = (SoapPrimitive) envelope.getResponse();
            //Assign it to fahren static variable
            String responseFromService = response.toString();
            System.out.println("Response from Recieve " + responseFromService);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }


    public void insertRegId(Registration registration) {
        //Create request
        SoapObject request = new SoapObject(Constants.NAMESPACE, Constants.REGID_METHOD_NAME);
        //Property which holds input parameters
        PropertyInfo regId = new PropertyInfo();
        //Set Name
        regId.setName("RegID");
        //Set Value
        regId.setValue(registration.getRegId());
        //Set dataType
        regId.setType(String.class);
        //Add the property to request object

        //Property which holds input parameters
        PropertyInfo emlId = new PropertyInfo();
        //Set Name
        emlId.setName("emailid");
        //Set Value
        emlId.setValue(registration.getEmailId());
        //Set dataType
        emlId.setType(String.class);
        //Add the property to request object


        //Property which holds input parameters
        PropertyInfo facebookId = new PropertyInfo();
        //Set Name
        facebookId.setName("fbid");
        //Set Value
        facebookId.setValue(registration.getFbId());
        //Set dataType
        facebookId.setType(String.class);

        PropertyInfo appVersion = new PropertyInfo();
        //Set Name
        appVersion.setName("appVersion");
        //Set Value
        appVersion.setValue(registration.getAppVersion());
        //Set dataType
        appVersion.setType(String.class);


        //Add the property to request object
        request.addProperty(regId);
        request.addProperty(emlId);
        request.addProperty(facebookId);
        request.addProperty(appVersion);

        //Create envelope
        SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(
                SoapEnvelope.VER11);
        envelope.dotNet = true;
        //Set output SOAP object
        envelope.setOutputSoapObject(request);


        //Create HTTP call object
        HttpTransportSE androidHttpTransport = new HttpTransportSE(Constants.REGID_URL);

        try {
            //Invole web service
            androidHttpTransport.call(Constants.REGID_SOAP_ACTION, envelope);
            //Get the response
            SoapPrimitive response = (SoapPrimitive) envelope.getResponse();
            //Assign it to fahren static variable
            String responseFromService = response.toString();
            System.out.println("Response for regId" + responseFromService);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public String sendContactDetails(String userNumber) {
        //Create request
        SoapObject request = new SoapObject(Constants.NAMESPACE, Constants.CONTACT_METHOD_NAME);
        //Property which holds input parameters
        PropertyInfo regId = new PropertyInfo();
        //Set Name
        regId.setName("mynumID");
        //Set Value
        regId.setValue(userNumber);
        //Set dataType
        regId.setType(Integer.class);
        //Add the property to request object


        SoapObject lstUsers = new SoapObject(Constants.NAMESPACE, "lstusers");

        for (Contact contact : listOfContacts) {
            SoapObject clsAndroidUsers = new SoapObject(Constants.NAMESPACE, "clsAndroidUsers");
            clsAndroidUsers.addProperty("_friendname", contact.getName());
            clsAndroidUsers.addProperty("_friendnum", contact.getNumber());
            clsAndroidUsers.addProperty("_friendemail", contact.getEmail());
            /*Bitmap image = contact.getImage();
            if(image!=null)
			{
				ByteArrayOutputStream stream = new ByteArrayOutputStream();
				image.compress(Bitmap.CompressFormat.PNG, 100, stream);
				clsAndroidUsers.addProperty("_friendprofilepic",Base64.encode(stream.toByteArray()));
			}
			else
			{
				clsAndroidUsers.addProperty("_friendprofilepic",null);
			}*/

            lstUsers.addSoapObject(clsAndroidUsers);
        }
        //Add the property to request object
        request.addProperty(regId);
        request.addSoapObject(lstUsers);


        //Create envelope
        SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(
                SoapEnvelope.VER11);
        envelope.dotNet = true;
        //Set output SOAP object
        envelope.setOutputSoapObject(request);


        //Create HTTP call object
        HttpTransportSE androidHttpTransport = new HttpTransportSE(Constants.CONTACT_URL);

        try {
            //Invole web service
            androidHttpTransport.call(Constants.CONTACT_SOAP_ACTION, envelope);
            //Get the response
            SoapPrimitive response = (SoapPrimitive) envelope.getResponse();
            //Assign it to fahren static variable
            String responseFromService = response.toString();
            return responseFromService;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }





}
