package org.cathassist.bible.setting;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.MenuItem;

import org.cathassist.bible.App;
import org.cathassist.bible.R;
import org.cathassist.bible.lib.Para;


public class AboutUsActivity extends SherlockActivity {
    private ProgressBar mProgressBar;
    private WebView mWebView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(Para.THEME);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.about_us);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("关于我们");

        mProgressBar = (ProgressBar) findViewById(R.id.pb);

        mWebView = (WebView) findViewById(R.id.web_view);
        mWebView.setFocusable(true);
        mWebView.setBackgroundColor(0);
        mWebView.setScrollBarStyle(WebView.SCROLLBARS_INSIDE_OVERLAY);

        WebSettings setting = mWebView.getSettings();
        setting.setJavaScriptEnabled(true);
        setting.setSupportZoom(false);

        String url = "http://www.cathassist.org/3rd/aboutus.html";

        if (App.get().isWifi() || Para.allow_gprs) {
            mWebView.getSettings().setCacheMode(WebSettings.LOAD_DEFAULT);
            mWebView.setWebViewClient(new InsideWebViewClient());
            mWebView.setWebChromeClient(new WebChromeClient() {
                public void onProgressChanged(WebView view, int progress) {
                    if (progress < 100 && mProgressBar.getVisibility() == ProgressBar.GONE) {
                        mProgressBar.setVisibility(ProgressBar.VISIBLE);
                    }
                    mProgressBar.setProgress(progress);
                    if (progress == 100) {
                        mProgressBar.setVisibility(ProgressBar.GONE);
                        mProgressBar.setVisibility(View.GONE);
                    }
                }
            });
            mWebView.loadUrl(url);
        } else {
            mWebView.getSettings().setCacheMode(WebSettings.LOAD_CACHE_ELSE_NETWORK);
            Toast.makeText(this, "请在WIFI环境下加载\n或在设置中打开使用数据流量选项", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private class InsideWebViewClient extends WebViewClient {
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            view.loadUrl(url);
            return true;
        }

        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            super.onPageStarted(view, url, favicon);
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            super.onPageFinished(view, url);
            mProgressBar.setVisibility(ProgressBar.GONE);
        }

        @Override
        public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
            super.onReceivedError(view, errorCode, description, failingUrl);
        }
    }
}
