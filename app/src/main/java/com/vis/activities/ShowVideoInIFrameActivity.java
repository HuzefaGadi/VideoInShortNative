package com.vis.activities;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.vis.R;

public class ShowVideoInIFrameActivity extends AppCompatActivity {

    WebView webview;
    String videoId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_video_in_iframe);
        videoId = getIntent().getStringExtra("VIDEO_ID");
        webview = (WebView) findViewById(R.id.webView);
        showIframe();
    }

    private void showIframe() {

        final ProgressDialog dialog = new ProgressDialog(this);
        dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        dialog.setCancelable(false);
        dialog.setTitle("Loading..");
        dialog.setMessage("Please wait..");
        dialog.show();
        //  String frameVideo = "<html><body><iframe width=100% height=100% src=\"https://www.youtube.com/embed/"+videoId+"?&rel=0&fs=1&showinfo=0&autohide=1&vq=hd720&hd=1\" frameborder=\"0\" allowfullscreen webkitallowfullscreen mozallowfullscreen oallowfullscreen msallowfullscreen></iframe></body></html>";
        String frameVideo = "<html><body><iframe width=100% height=100% src=\"https://www.youtube.com/embed/" + videoId + "?rel=0&fs=1&vq=hd720&hd=1 allowfullscreen ></iframe></body></html>";
        webview.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                return false;
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                if(dialog!=null)
                {
                    dialog.dismiss();
                }
            }
        });
        WebSettings webSettings = webview.getSettings();
        webSettings.setJavaScriptEnabled(true);
        int SDK_INT = android.os.Build.VERSION.SDK_INT;
        if (SDK_INT > 16) {
            webSettings.setMediaPlaybackRequiresUserGesture(false);
        }
        webSettings.setPluginState(WebSettings.PluginState.ON);
        webview.setWebChromeClient(new WebChromeClient());
        webview.loadData(getHTML(), "text/html", "utf-8");
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (webview != null) {
            webview.onPause();
            webview.pauseTimers();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (webview != null) {
            webview.onResume();
            webview.resumeTimers();

        }
    }

    public String getHTML() {
        String html = "<iframe class=\"youtube-player\" style=\"border: 0; width: 100%; height: 95%; padding:0px; margin:0px\" id=\"ytplayer\" type=\"text/html\" src=\"https://www.youtube.com/embed/"
                + videoId
                + "?rel=0&fs=1&vq=hd720&hd=1 allowfullscreen\" frameborder=\"0\">\n"
                + "</iframe>\n";
        return html;
    }
}
