package com.vis.activities;

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

public class ShowVideoActivity extends Activity implements YouTubePlayer.OnFullscreenListener{

   VideoFragment videoFragment;
    String videoId;
   /*  YouTubePlayerView playerView;

    YouTubePlayer player;*/

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_video);
        videoId = getIntent().getStringExtra("VIDEO_ID");
       /* playerView = (YouTubePlayerView) findViewById(R.id.player);
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
    public void onFullscreen(boolean b) {

    }



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
