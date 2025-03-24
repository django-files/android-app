package com.djangofiles.djangofiles;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.KeyEvent;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.EditText;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class MainActivity extends AppCompatActivity {

    private static final String PREFS_NAME = "AppPreferences";
    private static final String URL_KEY = "saved_url";
    private WebView webView;

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        webView = findViewById(R.id.webview);
        webView.getSettings().setDomStorageEnabled(true);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.addJavascriptInterface(new WebAppInterface(this), "Android");
        webView.setWebViewClient(new MyWebViewClient());

        SharedPreferences preferences = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String savedUrl = preferences.getString(URL_KEY, null);

        if (savedUrl == null || savedUrl.isEmpty()) {
            showUrlInputDialog(preferences);
        } else {
            webView.loadUrl(savedUrl);
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    private void showUrlInputDialog(SharedPreferences preferences) {
        EditText input = new EditText(this);
        input.setHint("Example: df.cssnr.com");

        new AlertDialog.Builder(this)
                .setTitle("Your Django Files URL")
                .setView(input)
                .setCancelable(false)
                .setPositiveButton("Save", (dialog, which) -> {
                    String url = input.getText().toString().trim();
                    if (!url.startsWith("http://") && !url.startsWith("https://")) {
                        url = "https://" + url;
                    }
                    preferences.edit().putString(URL_KEY, url).apply();
                    webView.loadUrl(url);
                })
                .setNegativeButton("Exit", (dialog, which) -> finish())
                .show();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if ((keyCode == KeyEvent.KEYCODE_BACK) && webView.canGoBack()) {
            webView.goBack();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    private class MyWebViewClient extends WebViewClient {
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
            SharedPreferences preferences = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
            String savedUrl = preferences.getString(URL_KEY, null);
            String requestUrl = request.getUrl().toString();

            if ((savedUrl != null && requestUrl.startsWith(savedUrl)) ||
                    requestUrl.startsWith("https://discord.com/oauth2")) {
                return false;
            }

            Intent intent = new Intent(Intent.ACTION_VIEW, request.getUrl());
            startActivity(intent);
            return true;
        }
    }
}
