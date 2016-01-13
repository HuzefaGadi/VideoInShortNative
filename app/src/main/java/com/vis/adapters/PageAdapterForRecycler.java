package com.vis.adapters;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
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
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;
import com.vis.Analytics;
import com.vis.R;
import com.vis.activities.HashTagActivity;
import com.vis.activities.MainActivity;
import com.vis.activities.ShowVideoActivity;
import com.vis.activities.ShowVideoInIFrameActivity;
import com.vis.beans.FbProfile;
import com.vis.beans.VideoEntry;
import com.vis.beans.VideoViewBean;
import com.vis.utilities.Constants;
import com.vis.utilities.Utility;
import com.vis.utilities.WebServiceUtility;

import java.util.Collections;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by huzefaasger on 04-01-2016.
 */

public class PageAdapterForRecycler extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private List<VideoEntry> entries;
    private final LayoutInflater inflater;
    private static Context mContext;
    Utility utility;

    FbProfile fbProfile;
    private ImageLoadingListener imageLoadingListener;
    DisplayImageOptions options;
    ImageLoader imageLoader;
    int width, height;
    Tracker mTracker;
    String appPackageName;
    public CallbackManager callbackManager;
    private static final int TYPE_HEADER = 2;
    private static final int TYPE_ITEM = 1;

    public void setListEntries(List<VideoEntry> entries)
    {
        this.entries = entries;
    }
    public PageAdapterForRecycler(Context context, List<VideoEntry> entries, FbProfile fbProfile) {
        this.entries = entries;
        inflater = LayoutInflater.from(context);
        mContext = context;
        this.fbProfile = fbProfile;

        callbackManager = CallbackManager.Factory.create();
        utility = new Utility(mContext);
        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        width = displayMetrics.widthPixels;
        height = displayMetrics.heightPixels;

        if (width < height) {
            width = height;
        }
        width = (width * 80) / 100;
        height = (width * 60) / 100;

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

        if(mContext instanceof MainActivity)
        {
            mTracker = ((Analytics) ((MainActivity) mContext).getApplication()).getDefaultTracker();
        }
        else
        {
            mTracker = ((Analytics) ((HashTagActivity) mContext).getApplication()).getDefaultTracker();
        }


    }

    public void releaseLoaders() {
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        // each data item is just a string in this case
        ImageView thumbnail, videoPreviewPlayButton, fbShare, watsappShare;
        TextView label,hashTag1,hashTag2,hashTag3;

        public ViewHolder(View view) {
            super(view);
            thumbnail = (ImageView) view.findViewById(R.id.imagethumbnail);
            videoPreviewPlayButton = (ImageView) view.findViewById(R.id.videoPreviewPlayButton);
            label = ((TextView) view.findViewById(R.id.text));
            fbShare = (ImageView) view.findViewById(R.id.share_facebook);
            hashTag1 = (TextView)view.findViewById(R.id.hashtag1);
            hashTag2 = (TextView)view.findViewById(R.id.hashtag2);
            hashTag3 = (TextView)view.findViewById(R.id.hashtag3);
//ImageButton twitterShare = (ImageButton)view.findViewById(R.id.share_twitter);
            watsappShare = (ImageView) view.findViewById(R.id.share_watsapp);

        }
    }

    public class RecyclerHeaderViewHolder extends RecyclerView.ViewHolder {
        public RecyclerHeaderViewHolder(View itemView) {
            super(itemView);
        }
    }

    @Override
    public int getItemViewType(int position) {
        if (isPositionHeader(position)) {
            return TYPE_HEADER;
        }
        return TYPE_ITEM;
    }

    //added a method to check if given position is a header
    private boolean isPositionHeader(int position) {
        return position == 0;
    }
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        if (viewType == TYPE_ITEM) {
            View view = inflater.inflate(R.layout.video_list_item, parent, false);
            ViewHolder vh = new ViewHolder(view);
            return vh;
        } else if (viewType == TYPE_HEADER) {
            final View view = LayoutInflater.from(context).inflate(R.layout.header, parent, false);
            return new RecyclerHeaderViewHolder(view);
        }
        throw new RuntimeException("There is no type that matches the type " + viewType + " + make sure your using types correctly");
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder v, int position) {

        if (!isPositionHeader(position)) {
            VideoEntry entry = entries.get(position -1);
            ViewHolder view = (ViewHolder)v;
            view.thumbnail.getLayoutParams().height = height;
            view.thumbnail.getLayoutParams().width = width;
            view.thumbnail.requestLayout();

            view.videoPreviewPlayButton.getLayoutParams().height =(int) (height*.50);
            view.videoPreviewPlayButton.getLayoutParams().width = (int) (width * .50);
            view.videoPreviewPlayButton.requestLayout();
            view.fbShare.setTag(entry.getVideoId());
            //  twitterShare.setTag(entry.getVideoId());

            view.watsappShare.setTag(entry.getVideoId() + ";" + entry.getPostTitle());

            view.thumbnail.setTag(entry.getVideoId());

            view.thumbnail.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showFullScreenVideo((String) v.getTag());
                }
            });

            view.fbShare.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    shareOnFacebook((String) view.getTag());
                }
            });
            view.watsappShare.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    try {
                        String tags[] = ((String) view.getTag()).split(";");
                        shareOnWatsapp(tags[0], tags[1]);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });

            /*view.fbShare.setOnTouchListener(new View.OnTouchListener() {
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


            view.watsappShare.setOnTouchListener(new View.OnTouchListener() {
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
            });*/
            view.hashTag1.setText(entry.getHashTag1());
            view.hashTag2.setText(entry.getHashTag2());
            view.hashTag3.setText(entry.getHashTag3());
            view.hashTag1.setTag(entry.getHashTag1());
            view.hashTag2.setTag(entry.getHashTag2());
            view.hashTag3.setTag(entry.getHashTag3());

            view.hashTag1.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(mContext, HashTagActivity.class);
                    intent.putExtra(Constants.HASHTAG,String.valueOf(view.getTag()));
                    mContext.startActivity(intent);
                }
            });

            view.hashTag2.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(mContext, HashTagActivity.class);
                    intent.putExtra(Constants.HASHTAG, String.valueOf(view.getTag()));
                    mContext.startActivity(intent);
                }
            });

            view.hashTag3.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(mContext, HashTagActivity.class);
                    intent.putExtra(Constants.HASHTAG, String.valueOf(view.getTag()));
                    mContext.startActivity(intent);
                }
            });

            String youtubeTag = "http://img.youtube.com/vi/" + entry.getVideoId() + "/0.jpg";
            ImageSize targetSize = new ImageSize(width, height);
            imageLoader.displayImage(youtubeTag, view.thumbnail, options, imageLoadingListener);
            view.label.setText(entry.getPostTitle());
            view.label.setVisibility(View.VISIBLE);

        }
    }


    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public int getItemCount() {
        return entries.size() + 1;
    }

 /*  @Override
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
    }*/

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
        String userId= null;
        if(fbProfile!=null)
        {
            userId = fbProfile.getFbUserId();
        }
        String appUrl = "http://t.videoinshort.com/trackshareddata.aspx?UserId=" + userId + "&VideoId=" + videoAddress + "&Constant=f";
        //String appUrl = "https://play.google.com/store/apps/details?id=" + appPackageName;
        ShareDialog shareDialog;
        if(mContext instanceof MainActivity)
        {
            shareDialog = new ShareDialog((MainActivity) mContext);
        }
        else
        {
            shareDialog = new ShareDialog((HashTagActivity) mContext);
        }
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
            videoViewBean.setUserId(userId);
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
        String networkConstant = utility.connectedNetwork();

        VideoViewBean videoViewBean = new VideoViewBean();
        videoViewBean.setVideoId(videoId);
        videoViewBean.setUserId(fbProfile.getFbUserId());
        videoViewBean.setDate(new Date().toString());
        videoViewBean.setNetworkType(networkConstant);
        new WebServiceUtility(mContext, Constants.VIDEO_VIEW, videoViewBean);
        Intent intent = null;

        if (networkConstant != null && networkConstant.equals(Constants.WIFI)) {
            intent = new Intent(mContext, ShowVideoActivity.class);
        } else {
            intent = new Intent(mContext, ShowVideoInIFrameActivity.class);
        }

        intent.putExtra("VIDEO_ID", videoId);
        if(mContext instanceof MainActivity)
        {
            ((MainActivity) mContext).startActivityForResult(intent, 10);
        }
        else
        {
            ((HashTagActivity) mContext).startActivityForResult(intent, 10);
        }

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

