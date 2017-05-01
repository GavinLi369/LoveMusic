package gavin.lovemusic.musicnews;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.orhanobut.logger.Logger;

import gavin.lovemusic.constant.R;

/**
 * Created by GavinLi
 * on 4/6/17.
 */

public class NewsActivity extends AppCompatActivity {
    public static final String URL_KEY = "url";

    private WebView mWebView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_news);
        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));

        //TODO 华为G6 Android 4.4 iframe点击无响应
        mWebView = (WebView) findViewById(R.id.web_news);
        mWebView.getSettings().setJavaScriptEnabled(true);
        mWebView.getSettings().setDomStorageEnabled(true);
        mWebView.setWebViewClient(new WebViewClient() {
            //评论没有自动加载，在这里手动进行加载
            @Override
            public void onPageFinished(WebView view, String url) {
                mWebView.loadUrl("javascript:fetchComment()");
            }
        });
        String urlStr = getIntent().getStringExtra(URL_KEY);
        mWebView.loadUrl(urlStr);
    }

    @Override
    protected void onDestroy() {
        if(mWebView != null) mWebView.destroy();
        super.onDestroy();
    }
}
