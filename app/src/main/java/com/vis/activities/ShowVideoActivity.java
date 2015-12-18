package com.vis.activities;

import android.app.Activity;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.google.android.youtube.player.YouTubePlayer;
import com.vis.R;



import android.app.Activity;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.google.android.youtube.player.YouTubeBaseActivity;
import com.google.android.youtube.player.YouTubeInitializationResult;
import com.google.android.youtube.player.YouTubePlayer;
import com.google.android.youtube.player.YouTubePlayerView;
import com.vis.R;
import com.vis.fragments.VideoFragment;
import com.vis.utilities.DeveloperKey;

public class ShowVideoActivity extends AppCompatActivity implements YouTubePlayer.OnFullscreenListener{

   VideoFragment videoFragment;
    String videoId;

/*
YouTubePlayerView playerView;

    YouTubePlayer player;
*/


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_video);
        videoId = getIntent().getStringExtra("VIDEO_ID");

/*playerView = (YouTubePlayerView) findViewById(R.id.player);
        playerView.initialize(DeveloperKey.DEVELOPER_KEY, this);*/



         videoFragment =
                (VideoFragment) getFragmentManager().findFragmentById(R.id.video_fragment_container);
        videoFragment.setVideoId(videoId);


    }

    @Override
    public void onFullscreen(boolean b) {

    }
/*
    @Override
    public void onInitializationSuccess(YouTubePlayer.Provider provider, YouTubePlayer player, boolean wasRestored) {
        this.player = player;
        // Specify that we want to handle fullscreen behavior ourselves.
        this.player.addFullscreenControlFlag(YouTubePlayer.FULLSCREEN_FLAG_CUSTOM_LAYOUT);
        player.setOnFullscreenListener(this);
        if (!wasRestored) {
            player.loadVideo(videoId);
        }
        player.setFullscreen(true);
        player.play();

    }

    @Override
    public void onInitializationFailure(YouTubePlayer.Provider provider, YouTubeInitializationResult youTubeInitializationResult) {

    }

    @Override
    public void onDestroy() {
        if (player != null) {
            player.release();
        }
        super.onDestroy();
    }

    @Override
    public void onPause() {
        if (player != null) {
            player.pause();
        }
        super.onPause();
    }*/

}

/*
public class ShowVideoActivity extends AppCompatActivity implements YouTubePlayer.OnFullscreenListener{
    WebView displayVideo;
    private static final String URL = "file:///android_asset/index.html";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_video);
        String  videoId = getIntent().getStringExtra("VIDEO_ID");
        String frameVideo = "<html><body><iframe id=\'click\' width=100% height=100% src=\"https://www.youtube.com/embed/"+videoId+"?enablejsapi=1&playerapiid=ytplayer&version=3&rel=0&fs=1&showinfo=0&autohide=1&vq=hd720&hd=1\" frameborder=\"0\" allowfullscreen webkitallowfullscreen mozallowfullscreen oallowfullscreen msallowfullscreen></iframe></body></html>";

        displayVideo = (WebView)findViewById(R.id.webView);
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
    }
    @Override
    protected void onPause() {
        super.onPause();
        if (displayVideo != null) {
            displayVideo.onPause();
            displayVideo.pauseTimers();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (displayVideo != null) {
            displayVideo.onResume();
            displayVideo.resumeTimers();

        }
    }

    @Override
    public void onFullscreen(boolean b) {

    }
}
*/
