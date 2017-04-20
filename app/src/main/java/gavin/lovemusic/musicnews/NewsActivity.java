package gavin.lovemusic.musicnews;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.webkit.JsResult;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import gavin.lovemusic.constant.R;

/**
 * Created by GavinLi
 * on 4/6/17.
 */

public class NewsActivity extends AppCompatActivity {
    public static final String URL_KEY = "url";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_news);
        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));

        //TODO iframe无法显示
        WebView webView = (WebView) findViewById(R.id.web_news);
        webView.getSettings().setJavaScriptEnabled(true);
        String urlStr = getIntent().getStringExtra(URL_KEY);
        webView.loadUrl(urlStr);
    }
}
