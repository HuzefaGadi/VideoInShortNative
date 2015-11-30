package com.vis.adapters;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.share.model.ShareLinkContent;
import com.facebook.share.model.SharePhoto;
import com.facebook.share.model.ShareVideo;
import com.facebook.share.model.ShareVideoContent;
import com.facebook.share.widget.ShareDialog;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.google.android.youtube.player.YouTubeInitializationResult;
import com.google.android.youtube.player.YouTubeThumbnailLoader;
import com.google.android.youtube.player.YouTubeThumbnailView;
import com.nostra13.universalimageloader.cache.disc.naming.Md5FileNameGenerator;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.QueueProcessingType;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;
import com.vis.Analytics;
import com.vis.R;
import com.vis.activities.MainActivity;
import com.vis.activities.ShowVideoActivity;
import com.vis.beans.FbProfile;
import com.vis.beans.VideoEntry;
import com.vis.beans.VideoViewBean;
import com.vis.fragments.VideoFragment;
import com.vis.utilities.Constants;
import com.vis.utilities.DeveloperKey;
import com.vis.utilities.JavaScriptInterface;
import com.vis.utilities.WebServiceUtility;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Adapter for the video list. Manages a set of YouTubeThumbnailViews, including initializing each
 * of them only once and keeping track of the loader of each one. When the ListFragment gets
 * destroyed it releases all the loaders.
 */
public class PageAdapter extends BaseAdapter {

    private final List<VideoEntry> entries;
    private final LayoutInflater inflater;
    private static Context mContext;

    FbProfile fbProfile;
    private ImageLoadingListener animateFirstListener;
    DisplayImageOptions options;
    ImageLoader imageLoader;
    int width,height;
    Tracker mTracker;


    public PageAdapter(Context context, List<VideoEntry> entries , FbProfile fbProfile) {
        this.entries = entries;
        inflater = LayoutInflater.from(context);
        mContext = context;
        this.fbProfile = fbProfile;
        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        width = displayMetrics.widthPixels;
        height = (width * 75 )/100;
        animateFirstListener = new AnimateFirstDisplayListener();
        imageLoader =ImageLoader.getInstance();
        initImageLoader(context,imageLoader);
        options = new DisplayImageOptions.Builder()
                .showImageOnLoading(R.drawable.loading_spinner)
                .showImageForEmptyUri(R.drawable.ic_empty)
                .showImageOnFail(R.drawable.ic_error)
                .cacheInMemory(true)
                .cacheOnDisk(true)
                .considerExifParams(true)

                .build();
        mTracker= ((Analytics) ((MainActivity)mContext).getApplication()).getDefaultTracker();


    }

    public void releaseLoaders() {
    }



    @Override
    public int getCount() {
        return entries.size();
    }

    @Override
    public VideoEntry getItem(int position) {
        return entries.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = convertView;
        VideoEntry entry = entries.get(position);
        ImageView thumbnail,videoPreviewPlayButton;
        // There are three cases here
        if (view == null) {
            // 1) The view has not yet been created - we need to initialize the YouTubeThumbnailView.
            view = inflater.inflate(R.layout.video_list_item, parent, false);
            thumbnail = (ImageView) view.findViewById(R.id.imagethumbnail);

        } else {
             thumbnail = (ImageView) view.findViewById(R.id.imagethumbnail);

        }

        videoPreviewPlayButton = (ImageView)view.findViewById(R.id.videoPreviewPlayButton);

        thumbnail.getLayoutParams().height = height;
        thumbnail.getLayoutParams().width = width;
        thumbnail.requestLayout();

        videoPreviewPlayButton.getLayoutParams().height = height;
        videoPreviewPlayButton.getLayoutParams().width = width;
        videoPreviewPlayButton.requestLayout();

        TextView label = ((TextView) view.findViewById(R.id.text));
        ImageButton fbShare = (ImageButton)view.findViewById(R.id.share_facebook);
        //ImageButton twitterShare = (ImageButton)view.findViewById(R.id.share_twitter);
        ImageButton watsappShare = (ImageButton)view.findViewById(R.id.share_watsapp);
        fbShare.setTag(entry.getVideoId());
      //  twitterShare.setTag(entry.getVideoId());
        watsappShare.setTag(entry.getVideoId());
        thumbnail.setTag(entry.getVideoId());

        thumbnail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showFullScreenVideo((String) v.getTag());
            }
        });

        fbShare.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                shareOnFacebook((String) v.getTag());
            }
        });

        /*twitterShare.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                shareOnTwitter((String)v.getTag());
            }
        });*/

        watsappShare.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                shareOnWatsapp((String) v.getTag());
            }
        });

        String youtubeTag = "http://img.youtube.com/vi/" + entry.getVideoId() + "/0.jpg";
        imageLoader.displayImage(youtubeTag, thumbnail, options, animateFirstListener);
        label.setText(entry.getPostTitle());
        label.setVisibility(View.VISIBLE);
        return view;
    }

    private void shareOnTwitter(String videoId)
    {
        VideoViewBean videoViewBean = new VideoViewBean();
        videoViewBean.setVideoId(videoId);
        videoViewBean.setUserId(fbProfile.getFbUserId());
        videoViewBean.setType(Constants.TWITTER);
        new WebServiceUtility(mContext,Constants.SHARE_DATA,videoViewBean);

    }
    private void shareOnWatsapp(String videoId)
    {


        mTracker.enableAdvertisingIdCollection(true);
        // Build and send an Event.
        mTracker.send(new HitBuilders.EventBuilder()
                .setCategory("Share")
                .setAction("Shared")
                .setLabel("Watsapp Share")

                .build());
        try {
            String youtubeTag = "http://www.youtube.com/watch?v="+videoId;
            Intent sendIntent = new Intent();
            sendIntent.setAction(Intent.ACTION_SEND);
            sendIntent.putExtra(Intent.EXTRA_TEXT, youtubeTag);
            sendIntent.setType("text/plain");
            sendIntent.setPackage("com.whatsapp");
            mContext.startActivity(sendIntent);
            VideoViewBean videoViewBean = new VideoViewBean();
            videoViewBean.setVideoId(videoId);
            videoViewBean.setUserId(fbProfile.getFbUserId());
            videoViewBean.setType(Constants.WATSAPP);
            new WebServiceUtility(mContext,Constants.SHARE_DATA,videoViewBean);
        } catch (Exception e) {
            Toast.makeText(mContext,"Watsapp not found in your device",Toast.LENGTH_LONG).show();
        }
    }

    public void shareOnFacebook(String videoAddress) {


        mTracker.enableAdvertisingIdCollection(true);
        // Build and send an Event.
        mTracker.send(new HitBuilders.EventBuilder()
                .setCategory("Share")
                .setAction("Shared")
                .setLabel("facebook Share")

                .build());
       ShareDialog shareDialog = new ShareDialog((MainActivity)mContext);
        String youtubeTag = "http://www.youtube.com/watch?v="+videoAddress;
        String id = videoAddress;
        if (ShareDialog.canShow(ShareLinkContent.class)) {

            VideoViewBean videoViewBean = new VideoViewBean();
            videoViewBean.setVideoId(videoAddress);
            videoViewBean.setUserId(fbProfile.getFbUserId());
            videoViewBean.setType(Constants.FACEBOOK);
            new WebServiceUtility(mContext,Constants.SHARE_DATA,videoViewBean);

            ShareVideoContent videoContent = new ShareVideoContent.Builder()
                    .setContentUrl(Uri.parse(youtubeTag))
                    .setContentTitle("Video in Short")
                    .setPreviewPhoto(new SharePhoto.Builder().setImageUrl(Uri.parse("http://img.youtube.com/vi/" + id + "/0.jpg")).build())
                    .build();
           /* String imageUrl = "http://img.youtube.com/vi/" + videoAddress + "/0.jpg";
            ShareLinkContent linkContent = new ShareLinkContent.Builder()
                    .setContentTitle("Video in Short")
                    .setImageUrl(Uri.parse("http://img.youtube.com/vi/" + id + "/0.jpg"))
                    .setContentUrl(Uri.parse(youtubeTag))
                    .build();*/



            Uri videoFileUri = Uri.parse(youtubeTag);
            ShareVideo video = new ShareVideo.Builder()
                    .setLocalUrl(videoFileUri)
                    .build();
            ShareVideoContent content = new ShareVideoContent.Builder()
                    .setVideo(video)
                    .build();

            shareDialog.show(content);

        }

    }
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR1)
    private void showFullScreenVideo(String videoId)
    {
        VideoViewBean videoViewBean = new VideoViewBean();
        videoViewBean.setVideoId(videoId);
        videoViewBean.setUserId(fbProfile.getFbUserId());
        videoViewBean.setDate(new Date().toString());
        new WebServiceUtility(mContext, Constants.VIDEO_VIEW,videoViewBean);
        Intent intent = new Intent(mContext, ShowVideoActivity.class);
        intent.putExtra("VIDEO_ID",videoId);
        ((MainActivity)mContext).startActivityForResult(intent, 10);
    }

    public static void initImageLoader(Context context,ImageLoader imageLoader) {
        // This configuration tuning is custom. You can tune every option, you may tune some of them,
        // or you can create default configuration by
        //  ImageLoaderConfiguration.createDefault(this);
        // method.
        ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(context)
                .threadPriority(Thread.NORM_PRIORITY - 2)
                .denyCacheImageMultipleSizesInMemory()
                .diskCacheFileNameGenerator(new Md5FileNameGenerator())
                .diskCacheSize(50 * 1024 * 1024) // 50 Mb
                .tasksProcessingOrder(QueueProcessingType.LIFO)
                .writeDebugLogs() // Remove for release app

                .build();
        // Initialize ImageLoader with configuration.
        imageLoader.init(config);
    }



    private static class AnimateFirstDisplayListener extends SimpleImageLoadingListener {

        static final List<String> displayedImages = Collections.synchronizedList(new LinkedList<String>());

        @Override
        public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
            if (loadedImage != null) {
                ImageView imageView = (ImageView) view;
                boolean firstDisplay = !displayedImages.contains(imageUri);

                if (firstDisplay) {
                    FadeInBitmapDisplayer.animate(imageView, 500);
                    displayedImages.add(imageUri);
                }
            }
        }

        @Override
        public void onLoadingStarted(String imageUri, View view) {
            super.onLoadingStarted(imageUri, view);
        }
    }
}