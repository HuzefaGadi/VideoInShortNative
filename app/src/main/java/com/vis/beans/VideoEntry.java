package com.vis.beans;

/**
 * Created by Rashida on 27/11/15.
 */
public class VideoEntry {

    private String postId;
    private String postTitle;
    private String videoId;
    private String hashTag1;
    private String hashTag2;
    private String hashTag3;

    public String getHashTag1() {
        return hashTag1;
    }

    public void setHashTag1(String hashTag1) {
        this.hashTag1 = hashTag1;
    }

    public String getHashTag2() {
        return hashTag2;
    }

    public void setHashTag2(String hashTag2) {
        this.hashTag2 = hashTag2;
    }

    public String getHashTag3() {
        return hashTag3;
    }

    public void setHashTag3(String hashTag3) {
        this.hashTag3 = hashTag3;
    }

    public String getPostId() {
        return postId;
    }

    public void setPostId(String postId) {
        this.postId = postId;
    }

    public String getPostTitle() {
        return postTitle;
    }

    public void setPostTitle(String postTitle) {
        this.postTitle = postTitle;
    }

    public String getVideoId() {
        return videoId;
    }

    public void setVideoId(String videoId) {
        this.videoId = videoId;
    }
}
