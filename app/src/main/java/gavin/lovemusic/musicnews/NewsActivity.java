package gavin.lovemusic.musicnews;

import android.net.http.SslError;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.webkit.SslErrorHandler;
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

        //TODO iframe无法显示,Js加载的图片无法显示
        WebView webView = (WebView) findViewById(R.id.web_news);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setLoadsImagesAutomatically(true);
        String urlStr = getIntent().getStringExtra(URL_KEY);
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
                Toast.makeText(NewsActivity.this, "网络连接出错",
                        Toast.LENGTH_SHORT).show();
            }
        });
        webView.loadUrl(urlStr);
    }
}
