package com.vis.fragments;

import android.os.Bundle;

import com.google.android.youtube.player.YouTubeInitializationResult;
import com.google.android.youtube.player.YouTubePlayer;
import com.google.android.youtube.player.YouTubePlayerFragment;

import com.google.android.youtube.player.YouTubeThumbnailView;
import com.vis.activities.MainActivity;
import com.vis.activities.ShowVideoActivity;
import com.vis.utilities.DeveloperKey;


public class VideoFragment extends YouTubePlayerFragment
        implements YouTubePlayer.OnInitializedListener {

    private YouTubePlayer player;
    private String videoId;

    public YouTubePlayer getPlayer() {
        return player;
    }

    public void setPlayer(YouTubePlayer player) {
        this.player = player;
    }

    public String getVideoId() {
        return videoId;
    }

    public static VideoFragment newInstance() {
        return new VideoFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        initialize(DeveloperKey.DEVELOPER_KEY, this );
    }

    @Override
    public void onDestroy() {
        if (player != null) {
            player.release();
        }
        super.onDestroy();
    }

    public void setVideoId(String videoId) {
        if (videoId != null && !videoId.equals(this.videoId)) {
            this.videoId = videoId;
            if (player != null) {
                player.cueVideo(videoId);

            }
        }
    }

    public void pause() {
        if (player != null) {
            player.pause();
        }
    }

    @Override
    public void onInitializationSuccess(YouTubePlayer.Provider provider, YouTubePlayer player, boolean restored) {
        this.player = player;

        player.addFullscreenControlFlag(YouTubePlayer.FULLSCREEN_FLAG_ALWAYS_FULLSCREEN_IN_LANDSCAPE);
        player.setOnFullscreenListener((ShowVideoActivity) getActivity());
        if (!restored && videoId != null) {
            player.loadVideo(videoId);

        }
        player.setFullscreen(true);

        player.play();
    }

    @Override
    public void onInitializationFailure(YouTubePlayer.Provider provider, YouTubeInitializationResult result) {
        this.player = null;
    }

}