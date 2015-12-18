/*package com.vis.adapters;

import android.annotation.TargetApi;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.telecom.Call;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.share.Sharer;
import com.facebook.share.model.ShareLinkContent;
import com.facebook.share.widget.ShareDialog;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.nostra13.universalimageloader.cache.disc.naming.Md5FileNameGenerator;
import com.nostra13.universalimageloader.cache.memory.impl.WeakMemoryCache;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.assist.ImageSize;
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
import com.vis.utilities.Constants;
import com.vis.utilities.WebServiceUtility;

import java.util.Collections;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

*//**
 * Adapter for the video list. Manages a set of YouTubeThumbnailViews, including initializing each
 * of them only once and keeping track of the loader of each one. When the ListFragment gets
 * destroyed it releases all the loaders.
 *//*
public class PageAdapter extends BaseAdapter {

    private final List<VideoEntry> entries;
    private final LayoutInflater inflater;
    private static Context mContext;

    FbProfile fbProfile;
    private ImageLoadingListener imageLoadingListener;
    DisplayImageOptions options;
    ImageLoader imageLoader;
    int width, height;
    Tracker mTracker;
    String appPackageName;
    public CallbackManager callbackManager;

    public PageAdapter(Context context, List<VideoEntry> entries, FbProfile fbProfile) {
        this.entries = entries;
        inflater = LayoutInflater.from(context);
        mContext = context;
        this.fbProfile = fbProfile;
        callbackManager = CallbackManager.Factory.create();

        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        width = displayMetrics.widthPixels;
        height = displayMetrics.heightPixels;

        if(width<height)
        {
            width=height;
        }
        width = (width * 60 )/100;
        height = (width * 70) / 100;

        imageLoadingListener = new ImageLoadingListener();
        imageLoader = ImageLoader.getInstance();
        initImageLoader(context, imageLoader);
        appPackageName = mContext.getPackageName();
        options = new DisplayImageOptions.Builder()
                .showImageOnLoading(R.drawable.loading_spinner)
                .showImageForEmptyUri(R.drawable.ic_empty)
                .showImageOnFail(R.drawable.ic_error)
                .cacheInMemory(true)
                .cacheOnDisk(true)
                .considerExifParams(true)
                .bitmapConfig(Bitmap.Config.RGB_565)
                .resetViewBeforeLoading(true)
                .imageScaleType(ImageScaleType.EXACTLY)
                .build();
        mTracker = ((Analytics) ((MainActivity) mContext).getApplication()).getDefaultTracker();


    }

    public void releaseLoaders() {
    }

    @Override
    public boolean isEnabled(int position) {
        //Set a Toast or Log over here to check.
        return true;
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
        ImageView thumbnail;
        WebView displayVideo;
        // There are three cases here
        if (view == null) {
            // 1) The view has not yet been created - we need to initialize the YouTubeThumbnailView.
            view = inflater.inflate(R.layout.video_list_item, parent, false);

        }
        displayVideo = (WebView)view.findViewById(R.id.imageFrame);



        displayVideo.getLayoutParams().height = height;
        displayVideo.getLayoutParams().width = width;
        displayVideo.requestLayout();


        TextView label = ((TextView) view.findViewById(R.id.text));
        final ImageView fbShare = (ImageView) view.findViewById(R.id.share_facebook);
        //ImageButton twitterShare = (ImageButton)view.findViewById(R.id.share_twitter);
        ImageView watsappShare = (ImageView) view.findViewById(R.id.share_watsapp);
        fbShare.setTag(entry.getVideoId());
        //  twitterShare.setTag(entry.getVideoId());

        watsappShare.setTag(entry.getVideoId() + ";" + entry.getPostTitle());

        displayVideo.setTag(entry.getVideoId());

       *//* thumbnail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showFullScreenVideo((String) v.getTag());
            }
        });*//*
        String frameVideo = "<html><body><iframe id=\'click\' width=100% height=100% src=\"https://www.youtube.com/embed/"+entry.getVideoId()+"?enablejsapi=1&playerapiid=ytplayer&version=3&rel=0&fs=1&showinfo=0&autohide=1&vq=hd720&hd=1\" frameborder=\"0\" allowfullscreen webkitallowfullscreen mozallowfullscreen oallowfullscreen msallowfullscreen></iframe></body></html>";


        displayVideo.setWebViewClient(new WebViewClient(){
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                return false;
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);

            }
        });
        WebSettings webSettings = displayVideo.getSettings();
        webSettings.setJavaScriptEnabled(true);
        int SDK_INT = android.os.Build.VERSION.SDK_INT;
        if (SDK_INT > 16) {
            webSettings.setMediaPlaybackRequiresUserGesture(false);
        }

        webSettings.setPluginState(WebSettings.PluginState.ON);

        displayVideo.setWebChromeClient(new WebChromeClient());
        // displayVideo.loadUrl(URL);

        displayVideo.loadData(frameVideo, "text/html", "utf-8");
*//*
        fbShare.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });*//*

        fbShare.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        ImageView view = (ImageView) v;
                        view.setColorFilter(Color.argb(255, 255, 255, 255)); // White Tint
                        shareOnFacebook((String) view.getTag());
                        v.invalidate();
                        return true; // if you want to handle the touch event
                    case MotionEvent.ACTION_UP:
                    case MotionEvent.ACTION_CANCEL: {
                        ImageView view2 = (ImageView) v;
                        //clear the overlay
                        view2.getDrawable().clearColorFilter();
                        view2.invalidate();
                        return true;
                    }
                }
                return false;
            }
        });
        *//*twitterShare.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                shareOnTwitter((String)v.getTag());
            }
        });*//*

        watsappShare.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        ImageView view = (ImageView) v;
                        view.setColorFilter(Color.argb(255, 255, 255, 255)); // White Tint
                        try {
                            String tags[] = ((String) view.getTag()).split(";");
                            shareOnWatsapp(tags[0], tags[1]);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        v.invalidate();
                        return true; // if you want to handle the touch event
                    case MotionEvent.ACTION_UP:
                    case MotionEvent.ACTION_CANCEL: {
                        ImageView view2 = (ImageView) v;
                        //clear the overlay
                        view2.getDrawable().clearColorFilter();
                        view2.invalidate();
                        return true;
                    }
                }
                return false;
            }
        });



        label.setText(entry.getPostTitle());
        label.setVisibility(View.VISIBLE);
        return view;
    }

    private void shareOnTwitter(String videoId) {
        VideoViewBean videoViewBean = new VideoViewBean();
        videoViewBean.setVideoId(videoId);
        videoViewBean.setUserId(fbProfile.getFbUserId());
        videoViewBean.setType(Constants.TWITTER);
        new WebServiceUtility(mContext, Constants.SHARE_DATA, videoViewBean);

    }

    private void shareOnWatsapp(String videoId, String videoTitle) {
        ProgressDialog shareProgressDialog = new ProgressDialog(mContext);

        try {

            shareProgressDialog.setCancelable(false);
            shareProgressDialog.setMessage("Please wait..");
            shareProgressDialog.setTitle("Sharing..");
            shareProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            shareProgressDialog.show();
            String message = "To watch " + videoTitle + ", install Vint app https://play.google.com/store/apps/details?id=" + appPackageName;
            Intent sendIntent = new Intent();
            sendIntent.setAction(Intent.ACTION_SEND);
            sendIntent.putExtra(Intent.EXTRA_TEXT, message);
            sendIntent.setType("text/plain");
            sendIntent.setPackage("com.whatsapp");
            mContext.startActivity(sendIntent);
            VideoViewBean videoViewBean = new VideoViewBean();
            videoViewBean.setVideoId(videoId);
            videoViewBean.setUserId(fbProfile.getFbUserId());
            videoViewBean.setType(Constants.WATSAPP);
            new WebServiceUtility(mContext, Constants.SHARE_DATA, videoViewBean);

            mTracker.enableAdvertisingIdCollection(true);
            // Build and send an Event.
            mTracker.send(new HitBuilders.EventBuilder()
                    .setCategory("Share")
                    .setAction("Shared")
                    .setLabel("Watsapp Share")

                    .build());
        } catch (Exception e) {
            Toast.makeText(mContext, "Watsapp not found in your device", Toast.LENGTH_LONG).show();
        }

        if(shareProgressDialog!=null)
        {
            shareProgressDialog.dismiss();
        }
        *//*VideoViewBean videoViewBean = new VideoViewBean();
        videoViewBean.setVideoId(videoId);
        videoViewBean.setUserId(fbProfile.getFbUserId());
        videoViewBean.setType(Constants.WATSAPP);
        new WebServiceUtility(mContext, Constants.SHARE_DATA, videoViewBean);*//*



    }

    public void shareOnFacebook(String videoAddress) {
        final ProgressDialog shareProgressDialog = new ProgressDialog(mContext);
        shareProgressDialog.setCancelable(false);
        shareProgressDialog.setMessage("Please wait..");
        shareProgressDialog.setTitle("Sharing..");
        shareProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        shareProgressDialog.show();
        String appUrl = "https://play.google.com/store/apps/details?id=" + appPackageName;
        ShareDialog shareDialog = new ShareDialog((MainActivity) mContext);
        if (ShareDialog.canShow(ShareLinkContent.class)) {

            String imageUrl = "http://img.youtube.com/vi/" + videoAddress + "/0.jpg";
            ShareLinkContent linkContent = new ShareLinkContent.Builder()
                    .setContentTitle("Video in Short")
                    .setImageUrl(Uri.parse(imageUrl))
                    .setContentUrl(Uri.parse(appUrl))
                    .build();
            shareDialog.registerCallback(callbackManager, new FacebookCallback<Sharer.Result>() {
                @Override
                public void onSuccess(Sharer.Result result) {
                    shareProgressDialog.dismiss();
                }

                @Override
                public void onCancel() {
                    shareProgressDialog.dismiss();
                }

                @Override
                public void onError(FacebookException error) {
                    shareProgressDialog.dismiss();
                }
            });
            shareDialog.show(linkContent);

            VideoViewBean videoViewBean = new VideoViewBean();
            videoViewBean.setVideoId(videoAddress);
            videoViewBean.setUserId(fbProfile.getFbUserId());
            videoViewBean.setType(Constants.FACEBOOK);
            new WebServiceUtility(mContext, Constants.SHARE_DATA, videoViewBean);


            mTracker.enableAdvertisingIdCollection(true);
            // Build and send an Event.
            mTracker.send(new HitBuilders.EventBuilder()
                    .setCategory("Share")
                    .setAction("Shared")
                    .setLabel("facebook Share")
                    .build());
        }

    }



    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR1)
    private void showFullScreenVideo(String videoId) {
        VideoViewBean videoViewBean = new VideoViewBean();
        videoViewBean.setVideoId(videoId);
        videoViewBean.setUserId(fbProfile.getFbUserId());
        videoViewBean.setDate(new Date().toString());
        new WebServiceUtility(mContext, Constants.VIDEO_VIEW, videoViewBean);
        Intent intent = new Intent(mContext, ShowVideoActivity.class);
        intent.putExtra("VIDEO_ID", videoId);
        ((MainActivity) mContext).startActivityForResult(intent, 10);
    }

    public static void initImageLoader(Context context, ImageLoader imageLoader) {
        // This configuration tuning is custom. You can tune every option, you may tune some of them,
        // or you can create default configuration by
        //  ImageLoaderConfiguration.createDefault(this);
        // method.
        ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(context)
                .threadPriority(Thread.NORM_PRIORITY - 2)
                .denyCacheImageMultipleSizesInMemory()
                .diskCacheFileNameGenerator(new Md5FileNameGenerator())
                .diskCacheSize(20 * 1024 * 1024) // 20 Mb
                .tasksProcessingOrder(QueueProcessingType.LIFO)
                .writeDebugLogs() // Remove for release app

                .build();
        // Initialize ImageLoader with configuration.
        imageLoader.init(config);
    }


    private static class ImageLoadingListener extends SimpleImageLoadingListener {

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
}*/


package com.vis.adapters;

import android.annotation.TargetApi;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.share.Sharer;
import com.facebook.share.model.ShareLinkContent;
import com.facebook.share.widget.ShareDialog;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.nostra13.universalimageloader.cache.disc.naming.Md5FileNameGenerator;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.assist.ImageSize;
import com.nostra13.universalimageloader.core.assist.QueueProcessingType;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;
import com.vis.Analytics;
import com.vis.R;
import com.vis.activities.MainActivity;
import com.vis.activities.ShowVideoActivity;
import com.vis.beans.FbProfile;
import com.vis.beans.VideoEntry;
import com.vis.beans.VideoViewBean;
import com.vis.utilities.Constants;
import com.vis.utilities.WebServiceUtility;

import java.util.Collections;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;


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
    private ImageLoadingListener imageLoadingListener;
    DisplayImageOptions options;
    ImageLoader imageLoader;
    int width, height;
    Tracker mTracker;
    String appPackageName;
    public CallbackManager callbackManager;

    public PageAdapter(Context context, List<VideoEntry> entries, FbProfile fbProfile) {
        this.entries = entries;
        inflater = LayoutInflater.from(context);
        mContext = context;
        this.fbProfile = fbProfile;
        callbackManager = CallbackManager.Factory.create();

        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        width = displayMetrics.widthPixels;
        height = displayMetrics.heightPixels;

        if (width < height) {
            width = height;
        }
        width = (width * 60) / 100;
        height = (width * 70) / 100;

        imageLoadingListener = new ImageLoadingListener();
        imageLoader = ImageLoader.getInstance();
        initImageLoader(context, imageLoader);
        appPackageName = mContext.getPackageName();
        options = new DisplayImageOptions.Builder()
                .showImageOnLoading(R.drawable.loading_spinner)
                .showImageForEmptyUri(R.drawable.ic_empty)
                .showImageOnFail(R.drawable.ic_error)
                .cacheInMemory(true)
                .cacheOnDisk(true)
                .considerExifParams(true)
                .bitmapConfig(Bitmap.Config.RGB_565)
                .resetViewBeforeLoading(true)
                .imageScaleType(ImageScaleType.EXACTLY)
                .build();
        mTracker = ((Analytics) ((MainActivity) mContext).getApplication()).getDefaultTracker();


    }

    public void releaseLoaders() {
    }

    @Override
    public boolean isEnabled(int position) {
        //Set a Toast or Log over here to check.
        return true;
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
        ImageView thumbnail, videoPreviewPlayButton;
        // There are three cases here
        if (view == null) {
            // 1) The view has not yet been created - we need to initialize the YouTubeThumbnailView.
            view = inflater.inflate(R.layout.video_list_item, parent, false);
            thumbnail = (ImageView) view.findViewById(R.id.imagethumbnail);

        } else {
            thumbnail = (ImageView) view.findViewById(R.id.imagethumbnail);

        }

        videoPreviewPlayButton = (ImageView) view.findViewById(R.id.videoPreviewPlayButton);

        thumbnail.getLayoutParams().height = height;
        thumbnail.getLayoutParams().width = width;
        thumbnail.requestLayout();

        videoPreviewPlayButton.getLayoutParams().height = height;
        videoPreviewPlayButton.getLayoutParams().width = width;
        videoPreviewPlayButton.requestLayout();

        TextView label = ((TextView) view.findViewById(R.id.text));
        final ImageView fbShare = (ImageView) view.findViewById(R.id.share_facebook);
        //ImageButton twitterShare = (ImageButton)view.findViewById(R.id.share_twitter);
        ImageView watsappShare = (ImageView) view.findViewById(R.id.share_watsapp);
        fbShare.setTag(entry.getVideoId());
        //  twitterShare.setTag(entry.getVideoId());

        watsappShare.setTag(entry.getVideoId() + ";" + entry.getPostTitle());

        thumbnail.setTag(entry.getVideoId());

        thumbnail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showFullScreenVideo((String) v.getTag());
            }
        });


        fbShare.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        ImageView view = (ImageView) v;
                        view.setColorFilter(Color.argb(255, 255, 255, 255)); // White Tint
                        shareOnFacebook((String) view.getTag());
                        v.invalidate();
                        return true; // if you want to handle the touch event
                    case MotionEvent.ACTION_UP:
                    case MotionEvent.ACTION_CANCEL: {
                        ImageView view2 = (ImageView) v;
                        //clear the overlay
                        view2.getDrawable().clearColorFilter();
                        view2.invalidate();
                        return true;
                    }
                }
                return false;
            }
        });


        watsappShare.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        ImageView view = (ImageView) v;
                        view.setColorFilter(Color.argb(255, 255, 255, 255)); // White Tint
                        try {
                            String tags[] = ((String) view.getTag()).split(";");
                            shareOnWatsapp(tags[0], tags[1]);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        v.invalidate();
                        return true; // if you want to handle the touch event
                    case MotionEvent.ACTION_UP:
                    case MotionEvent.ACTION_CANCEL: {
                        ImageView view2 = (ImageView) v;
                        //clear the overlay
                        view2.getDrawable().clearColorFilter();
                        view2.invalidate();
                        return true;
                    }
                }
                return false;
            }
        });


        String youtubeTag = "http://img.youtube.com/vi/" + entry.getVideoId() + "/0.jpg";
        ImageSize targetSize = new ImageSize(width, height);
        imageLoader.displayImage(youtubeTag, thumbnail, options, imageLoadingListener);
        label.setText(entry.getPostTitle());
        label.setVisibility(View.VISIBLE);
        return view;
    }

    private void shareOnTwitter(String videoId) {
        VideoViewBean videoViewBean = new VideoViewBean();
        videoViewBean.setVideoId(videoId);
        videoViewBean.setUserId(fbProfile.getFbUserId());
        videoViewBean.setType(Constants.TWITTER);
        new WebServiceUtility(mContext, Constants.SHARE_DATA, videoViewBean);

    }

    private void shareOnWatsapp(String videoId, String videoTitle) {
        ProgressDialog shareProgressDialog = new ProgressDialog(mContext);

        try {
            shareProgressDialog.setCancelable(false);
            shareProgressDialog.setMessage("Please wait..");
            shareProgressDialog.setTitle("Sharing..");
            shareProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            shareProgressDialog.show();
            //   String message = "To watch " + videoTitle + ", install Vint app https://play.google.com/store/apps/details?id=" + appPackageName;
            String message = "To watch " + videoTitle + ", install Vint app http://t.videoinshort.com/trackshareddata.aspx?UserId=" + fbProfile.getFbUserId() + "&VideoId=" + videoId + "&Constant=w";

            Intent sendIntent = new Intent();
            sendIntent.setAction(Intent.ACTION_SEND);
            sendIntent.putExtra(Intent.EXTRA_TEXT, message);
            sendIntent.setType("text/plain");
            sendIntent.setPackage("com.whatsapp");
            mContext.startActivity(sendIntent);
            VideoViewBean videoViewBean = new VideoViewBean();
            videoViewBean.setVideoId(videoId);
            videoViewBean.setUserId(fbProfile.getFbUserId());
            videoViewBean.setType(Constants.WATSAPP);
            new WebServiceUtility(mContext, Constants.SHARE_DATA, videoViewBean);

            mTracker.enableAdvertisingIdCollection(true);
            // Build and send an Event.
            mTracker.send(new HitBuilders.EventBuilder()
                    .setCategory("Share")
                    .setAction("Shared")
                    .setLabel("Watsapp Share")

                    .build());
        } catch (Exception e) {
            Toast.makeText(mContext, "Watsapp not found in your device", Toast.LENGTH_LONG).show();
        }

        if (shareProgressDialog != null) {
            shareProgressDialog.dismiss();
        }
    }

    public void shareOnFacebook(String videoAddress) {
        final ProgressDialog shareProgressDialog = new ProgressDialog(mContext);
        shareProgressDialog.setCancelable(false);
        shareProgressDialog.setMessage("Please wait..");
        shareProgressDialog.setTitle("Sharing..");
        shareProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        shareProgressDialog.show();
        String appUrl = "http://t.videoinshort.com/trackshareddata.aspx?UserId=" + fbProfile.getFbUserId() + "&VideoId=" + videoAddress + "&Constant=f";
        //String appUrl = "https://play.google.com/store/apps/details?id=" + appPackageName;
        ShareDialog shareDialog = new ShareDialog((MainActivity) mContext);
        if (ShareDialog.canShow(ShareLinkContent.class)) {

            String imageUrl = "http://img.youtube.com/vi/" + videoAddress + "/0.jpg";
            ShareLinkContent linkContent = new ShareLinkContent.Builder()
                    .setContentTitle("Video in Short")
                    .setImageUrl(Uri.parse(imageUrl))
                    .setContentUrl(Uri.parse(appUrl))
                    .build();
            shareDialog.registerCallback(callbackManager, new FacebookCallback<Sharer.Result>() {
                @Override
                public void onSuccess(Sharer.Result result) {
                    shareProgressDialog.dismiss();
                }

                @Override
                public void onCancel() {
                    shareProgressDialog.dismiss();
                }

                @Override
                public void onError(FacebookException error) {
                    shareProgressDialog.dismiss();
                }
            });
            shareDialog.show(linkContent);

            VideoViewBean videoViewBean = new VideoViewBean();
            videoViewBean.setVideoId(videoAddress);
            videoViewBean.setUserId(fbProfile.getFbUserId());
            videoViewBean.setType(Constants.FACEBOOK);
            new WebServiceUtility(mContext, Constants.SHARE_DATA, videoViewBean);


            mTracker.enableAdvertisingIdCollection(true);
            // Build and send an Event.
            mTracker.send(new HitBuilders.EventBuilder()
                    .setCategory("Share")
                    .setAction("Shared")
                    .setLabel("facebook Share")
                    .build());
        }

    }


    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR1)
    private void showFullScreenVideo(String videoId) {
        VideoViewBean videoViewBean = new VideoViewBean();
        videoViewBean.setVideoId(videoId);
        videoViewBean.setUserId(fbProfile.getFbUserId());
        videoViewBean.setDate(new Date().toString());
        new WebServiceUtility(mContext, Constants.VIDEO_VIEW, videoViewBean);
        Intent intent = new Intent(mContext, ShowVideoActivity.class);
        intent.putExtra("VIDEO_ID", videoId);
        ((MainActivity) mContext).startActivityForResult(intent, 10);
    }

    public static void initImageLoader(Context context, ImageLoader imageLoader) {
        // This configuration tuning is custom. You can tune every option, you may tune some of them,
        // or you can create default configuration by
        //  ImageLoaderConfiguration.createDefault(this);
        // method.
        ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(context)
                .threadPriority(Thread.NORM_PRIORITY - 2)
                .denyCacheImageMultipleSizesInMemory()
                .diskCacheFileNameGenerator(new Md5FileNameGenerator())
                .diskCacheSize(20 * 1024 * 1024) // 20 Mb
                .tasksProcessingOrder(QueueProcessingType.LIFO)
                .writeDebugLogs() // Remove for release app

                .build();
        // Initialize ImageLoader with configuration.
        imageLoader.init(config);
    }


    private static class ImageLoadingListener extends SimpleImageLoadingListener {

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
