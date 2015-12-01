package com.vis.fragments;

/**
 * Created by Rashida on 27/11/15.
 */

import android.annotation.TargetApi;
import android.app.ListFragment;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.vis.R;
import com.vis.adapters.PageAdapter;
import com.vis.beans.FbProfile;
import com.vis.beans.VideoEntry;
import com.vis.beans.VideoViewBean;
import com.vis.utilities.Constants;
import com.vis.utilities.WebServiceUtility;

import org.ksoap2.SoapEnvelope;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpTransportSE;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * A fragment that shows a static list of videos.
 */
public class VideoListFragment extends ListFragment {

    private List<VideoEntry> VIDEO_LIST;
    SharedPreferences pref;
    FbProfile fbProfile;

    private PageAdapter adapter;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        new CallWebservice().execute();
        pref = getActivity().getSharedPreferences(Constants.PREFERENCES_NAME,
                Context.MODE_PRIVATE);
        String responseFromFb = pref.getString(Constants.FB_USER_INFO, null);
        if (responseFromFb != null) {
            fbProfile = new Gson().fromJson(responseFromFb, FbProfile.class);
}
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);


        getListView().setChoiceMode(ListView.CHOICE_MODE_SINGLE);


    }

    public void refresh()
    {
        new CallWebservice().execute();
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
                totalItemsHeight += item.getMeasuredHeight()+20;
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

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }



    public class CallWebservice extends AsyncTask<Void,Void,List<VideoEntry>>
    {
        ProgressDialog dialog;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
             dialog = new ProgressDialog(getActivity());
            dialog.setMessage("Please wait..");
            dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            dialog.show();
        }

        @Override
        protected List<VideoEntry>  doInBackground(Void... params) {
            return getVideosList();
        }

        @Override
        protected void onPostExecute(List<VideoEntry> videoEntries) {
            super.onPostExecute(videoEntries);
            VIDEO_LIST = videoEntries;
            adapter = new PageAdapter(getActivity(), VIDEO_LIST , fbProfile);
            setListAdapter(adapter);
            setListViewHeightBasedOnItems(getListView());
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
            return videosList;

        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }



}