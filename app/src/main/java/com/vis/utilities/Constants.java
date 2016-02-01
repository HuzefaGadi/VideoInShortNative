package com.vis.utilities;

/**
 * Created by huzefaasger on 07-09-2015.
 */
public class Constants {
    public static final String PREFERENCES_NAME = "preferences";
    public static final String FB_USER_INFO = "fbUserInfo";
    public static final String HASHTAG = "hashtag";

    public static final String EXTRA_MESSAGE = "message";
    public static final String PROPERTY_REG_ID = "registration_id";
    public static final String PROPERTY_APP_VERSION = "appVersion";
    public static final int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
    /**
     * Tag used on log messages.
     */
    public static final String TAG = "GCM";

    public static final String PREFERENCES_ALREADY_RATED = "ALREADYRATED";
    public static final String PREFERENCES_SHOW_ALARM = "SHOWALARM";
    public static final String PREFERENCES_INTEREST = "INTEREST";
    public static final String PREFERENCES_SELECTED_INTERESTS = "PREFERENCES_SELECTED_INTERESTS";


    public static final String QUIZ_FEED_URL = "http://www.videoinshort.com/todays-picks";
    //public static final String QUIZ_FEED_URL = "http://www.google.com";
    public static final String url = "http://www.videoinshort.com/todays-picks";
    //public static final String url = "http://www.timesofindia.com";

    public static final String target_url_prefix = "m.videoinshort.com";
    public static final String target_url_prefix2 = "www.videoinshort.com";
    public static final String terms_and_condition = "http://m1.buzzonn.com/PrivcyPolicy.aspx";


    public static final String REGID_URL = "http://service.videoinshort.com/savefbuserdata.asmx";
    public static final String REGID_SOAP_ACTION = "http://tempuri.org/ModifyMobileData";
    public static final String REGID_METHOD_NAME = "ModifyMobileData";

    public static final String NEW_USER_URL = "http://service.videoinshort.com/savefbuserdata.asmx";
    public static final String NEW_USER_SOAP_ACTION = "http://tempuri.org/SaveFacebookData";
    public static final String NEW_USER_METHOD_NAME = "SaveFacebookData";


    public static final String CONTACT_URL = "http://m1.buzzonn.com/BuzzonFBList.asmx";
    public static final String CONTACT_SOAP_ACTION = "http://tempuri.org/insertFBList";
    public static final String CONTACT_METHOD_NAME = "insertFBList";

    public static final String ACK_URL = "http://service.videoinshort.com/savefbuserdata.asmx";
    public static final String ACK_SOAP_ACTION = "http://tempuri.org/SendClickReceiveNotiFication";
    public static final String ACK_METHOD_NAME = "SendClickReceiveNotiFication";

    public static final String CLICK_ACK_URL = "http://service.videoinshort.com/savefbuserdata.asmx";
    public static final String CLICK_ACK_SOAP_ACTION = "http://tempuri.org/SendClickNotiFication";
    public static final String CLICK_ACK_METHOD_NAME = "SendClickNotiFication";

    public static final String ACTIVE_URL = "http://service.videoinshort.com/savefbuserdata.asmx";
    public static final String ACTIVE_SOAP_ACTION = "http://tempuri.org/AppActive";
    public static final String ACTIVE_METHOD_NAME = "AppActive";

    public static final String VERSION_URL = "http://service.videoinshort.com/savefbuserdata.asmx";
    public static final String VERSION_SOAP_ACTION = "http://tempuri.org/AppVersion";
    public static final String VERSION_METHOD_NAME = "AppVersion";

    public static final String VIDEOS_URL = "http://service.videoinshort.com/savefbuserdata.asmx";
    public static final String VIDEOS_SOAP_ACTION = "http://tempuri.org/SentListOfVideo";
    public static final String VIDEOS_METHOD_NAME = "SentListOfVideo";

    public static final String HASHTAG_VIDEOS_URL = "http://service.videoinshort.com/savefbuserdata.asmx";
    public static final String HASHTAG_VIDEOS_SOAP_ACTION = "http://tempuri.org/SendVideoListByHashTag";
    public static final String HASHTAG_VIDEOS_METHOD_NAME = "SendVideoListByHashTag";

    public static final String VIDEOS_VIEW_URL = "http://service.videoinshort.com/savefbuserdata.asmx";
    public static final String VIDEOS_VIEW_SOAP_ACTION = "http://tempuri.org/VideoViewData";
    public static final String VIDEOS_VIEW_METHOD_NAME = "VideoViewData";

    public static final String VIDEOS_SHARE_URL = "http://service.videoinshort.com/savefbuserdata.asmx";
    public static final String VIDEOS_SHARE_SOAP_ACTION = "http://tempuri.org/SharedData";
    public static final String VIDEOS_SHARE_METHOD_NAME = "SharedData";

    public static final String ERROR_LOG_URL = "http://service.videoinshort.com/savefbuserdata.asmx";
    public static final String ERROR_LOG_ACTION = "http://tempuri.org/LogError";
    public static final String ERROR_LOG_METHOD_NAME = "LogError";

    public static final String FEEDBACK_URL = "http://service.videoinshort.com/savefbuserdata.asmx";
    public static final String FEEDBACK_ACTION = "http://tempuri.org/UserFeedback";
    public static final String FEEDBACK_METHOD_NAME = "UserFeedback";

    public static final String HASHTAG_FOLLOW_UNFOLLOW_URL = "http://service.videoinshort.com/savefbuserdata.asmx";
    public static final String HASHTAG_FOLLOW_UNFOLLOW_ACTION = "http://tempuri.org/SaveFollowUnfollowHashtag";
    public static final String HASHTAG_FOLLOW_UNFOLLOW_METHOD_NAME = "SaveFollowUnfollowHashtag";

    public static final String INTEREST_LIST_URL = "http://service.videoinshort.com/savefbuserdata.asmx";
    public static final String INTEREST_LIST_ACTION = "http://tempuri.org/IntrestList";
    public static final String INTEREST_LIST_METHOD_NAME = "IntrestList";

    public static final String INTEREST_LIST_BY_ID_URL = "http://service.videoinshort.com/savefbuserdata.asmx";
    public static final String INTEREST_LIST_BY_ID_ACTION = "http://tempuri.org/SendIntrestListByUserId";
    public static final String INTEREST_LIST_BY_ID_METHOD_NAME = "SendIntrestListByUserId";

    public static final String MULTIPLE_HASHTAGS_VIDEOS_URL = "http://service.videoinshort.com/savefbuserdata.asmx";
    public static final String MULTIPLE_HASHTAGS_VIDEOS_SOAP_ACTION = "http://tempuri.org/videoListWithMulipleHashTag";
    public static final String MULTIPLE_HASHTAGS_VIDEOS_METHOD_NAME = "videoListWithMulipleHashTag";


    public static final String NAMESPACE = "http://tempuri.org/";
    public static final String MENU_SETTINGS = "menusettings";
    public static final int SEND_FACEBOOK_DATA = 1;
    public static final int SEND_APP_ACTIVE_DATA = 2;
    public static final int RECIEVE_INFO_TASK = 3;
    public static final int CLICK_INFO_TASK = 4;
    public static final int USER_INFO_TASK = 5;
    public static final int UPDATE_APP = 6;
    public static final String USER_AGENT_POSTFIX_WITH_FACEBOOK = "VideoInShortWithFacebook";
    public static final String USER_AGENT_POSTFIX_WITHOUT_FACEBOOK = "VideoInShort";
    public static final int VIDEO_VIEW = 7;
    public static final int SHARE_DATA = 8;
    public static final int FEEDBACK = 9 ;
    public static final int FOLLOW_UNFOLLOW = 10 ;
    public static final int REQUEST_CODE_FOR_INTEREST = 10 ;
    public static final int REQUEST_CODE_FOR_SHOW_FULLSCREEN_VIDEO= 11 ;

    public static final String WATSAPP="w";
    public static final String FACEBOOK="f";
    public static final String TWITTER="t";

    public static final String WIFI = "W";
    public static final String MOBILEDATA = "M";



}
