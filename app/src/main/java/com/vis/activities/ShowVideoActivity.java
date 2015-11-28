package com.vis.activities;

import android.app.Activity;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import com.google.android.youtube.player.YouTubePlayer;
import com.vis.R;
import com.vis.fragments.VideoFragment;

public class ShowVideoActivity extends Activity implements YouTubePlayer.OnFullscreenListener{

    VideoFragment videoFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_video);

        String videoId = getIntent().getStringExtra("VIDEO_ID");
         videoFragment =
                (VideoFragment) getFragmentManager().findFragmentById(R.id.video_fragment_container);
        videoFragment.setVideoId(videoId);

    }

    @Override
    public void onFullscreen(boolean b) {

    }
}
