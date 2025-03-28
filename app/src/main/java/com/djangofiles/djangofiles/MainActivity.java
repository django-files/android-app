package com.djangofiles.djangofiles;

import android.annotation.SuppressLint;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.util.Log;
import android.view.KeyEvent;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.EditText;
import android.widget.Toast;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Objects;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;


public class MainActivity extends AppCompatActivity {

    private static final String PREFS_NAME = "AppPreferences";
    private static final String URL_KEY = "saved_url";
    private static final String TOKEN_KEY = "auth_token";

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
        webView.getSettings().setUserAgentString("DjangoFiles Android");
        webView.addJavascriptInterface(new WebAppInterface(this), "Android");
        webView.setWebViewClient(new MyWebViewClient());

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Handle Intent
        Log.d("onCreate", "----- onCreate -----");
        Log.d("onCreate", "getAction: " + getIntent().getAction());
        Log.d("onCreate", "getData: " + getIntent().getData());
        Log.d("onCreate", "getExtras: " + getIntent().getExtras());
        handleIntent(getIntent());

        // // Token is handled in WebAppInterface via the websites main.js
        // webView.evaluateJavascript("getAuthToken();", null);
        // Log.d("onCreate", "document.getElementById('auth-token').value");
        // webView.evaluateJavascript("getAuthToken();", null);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if ((keyCode == KeyEvent.KEYCODE_BACK) && webView.canGoBack()) {
            webView.goBack();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onNewIntent(@NonNull Intent intent) {
        super.onNewIntent(intent);
        Log.d("onNewIntent", "intent: " + intent);
        handleIntent(intent);
    }

    private void handleIntent(Intent intent) {
        // TODO: Need to do some serious debugging on intent handling...
        Uri uri = intent.getData();
        Log.d("handleIntent", "uri: " + uri);

        // String mimeType = getContentResolver().getType(uri);
        String mimeType = intent.getType();
        Log.d("handleIntent", "mimeType: " + mimeType);

        String action = intent.getAction();
        Log.d("handleIntent", "action: " + action);

        if (Intent.ACTION_MAIN.equals(action)) {
            Log.d("handleIntent", "ACTION_MAIN");
            SharedPreferences preferences = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
            String savedUrl = preferences.getString(URL_KEY, null);
            Log.d("handleIntent", "savedUrl: " + savedUrl);
            String authToken = preferences.getString(TOKEN_KEY, null);
            Log.d("handleIntent", "authToken: " + authToken);

            String currentUrl = webView.getUrl();
            Log.d("handleIntent", "currentUrl: " + currentUrl);

            if (savedUrl == null || savedUrl.isEmpty()) {
                showSettingsDialog();
            } else {
                if (currentUrl == null) {
                    Log.d("handleIntent", "webView.loadUrl");
                    webView.loadUrl(savedUrl);
                } else {
                    Log.d("handleIntent", "SKIPPING  webView.loadUrl");
                }
            }
        } else if (Intent.ACTION_VIEW.equals(action)) {
            Log.d("handleIntent", "ACTION_VIEW");
            if (uri != null) {
                String scheme = uri.getScheme();
                Log.d("handleIntent", "scheme: " + scheme);
                String host = uri.getHost();
                Log.d("handleIntent", "host: " + host);
                if ("djangofiles".equals(scheme)) {
                    if ("serverlist".equals(host)) {
                        Log.d("handleIntent", "djangofiles://serverlist");
                        showSettingsDialog();
                    } else {
                        Toast.makeText(this, this.getString(R.string.tst_error) + ": Unknown DeepLink", Toast.LENGTH_SHORT).show();
                        Log.d("handleIntent", "Unknown DeepLink!");
                        finish();
                    }
                } else {
                    Log.d("handleIntent", "processSharedFile: " + uri);
                    processSharedFile(uri);
                }
            } else {
                Toast.makeText(this, this.getString(R.string.tst_error) + ": Unknown Intent", Toast.LENGTH_SHORT).show();
                Log.e("IntentDebug", "Unknown Intent!");
                finish();
            }
        } else if (Intent.ACTION_SEND.equals(action) && mimeType != null) {
            Log.d("handleIntent", "ACTION_SEND");
            if ("text/plain".equals(mimeType)) {
                Toast.makeText(this, this.getString(R.string.tst_not_implemented), Toast.LENGTH_SHORT).show();
            } else {
                Uri fileUri = intent.getParcelableExtra(Intent.EXTRA_STREAM);
                if (fileUri != null) {
                    processSharedFile(fileUri);
                }
            }
        } else if (Intent.ACTION_SEND_MULTIPLE.equals(action)) {
            Log.d("handleIntent", "ACTION_SEND_MULTIPLE");
            ArrayList<Uri> fileUris = intent.getParcelableArrayListExtra(Intent.EXTRA_STREAM);
            if (fileUris != null) {
                for (Uri fileUri : fileUris) {
                    processSharedFile(fileUri);
                }
            }
        }
    }

    private void showSettingsDialog() {
        SharedPreferences preferences = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String savedUrl = preferences.getString(URL_KEY, null);
        Log.d("showSettingsDialog", "savedUrl: " + savedUrl);

        EditText input = new EditText(this);
        input.setHint(this.getString(R.string.settings_input_place));
        if (savedUrl != null) {
            input.setText(savedUrl);
        }
        input.requestFocus();

        runOnUiThread(() -> {
            new AlertDialog.Builder(this)
                    .setCancelable(false)
                    .setTitle(this.getString(R.string.settings_title))
                    .setMessage(this.getString(R.string.settings_message))
                    .setView(input)
                    .setNegativeButton("Exit", (dialog, which) -> finish())
                    .setPositiveButton("OK", (dialog, which) -> {
                        String url = input.getText().toString().trim();
                        Log.d("showSettingsDialog", "setPositiveButton: url: " + url);
                        if (url.isEmpty()) {
                            // TODO: Need to add verification here and keep dialog open...
                            Toast.makeText(this, this.getString(R.string.tst_invalid_url), Toast.LENGTH_SHORT).show();
                            finish();
                            return;
                        }
                        if (!url.startsWith("http://") && !url.startsWith("https://")) {
                            url = "https://" + url;
                        }
                        if (url.endsWith("/")) {
                            url = url.substring(0, url.length() - 1);
                        }
                        if (!Objects.equals(savedUrl, url)) {
                            Log.d("showSettingsDialog", "Saving New URL.");
                            preferences.edit().putString(URL_KEY, url).apply();
                            webView.loadUrl(url);
                            dialog.dismiss();
                        } else {
                            Log.d("showSettingsDialog", "URL NOT Changed!");
                            finish();
                        }
                    })
                    .show();
        });
    }

    private void processSharedFile(Uri fileUri) {
        Log.d("processSharedFile", "fileUri: " + fileUri);
        SharedPreferences preferences = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String savedUrl = preferences.getString(URL_KEY, null);
        Log.d("processSharedFile", "savedUrl: " + savedUrl);
        String authToken = preferences.getString(TOKEN_KEY, null);
        Log.d("processSharedFile", "authToken: " + authToken);
        if (savedUrl == null || authToken == null) {
            // TODO: Show settings dialog here...
            Toast.makeText(this, this.getString(R.string.tst_no_url), Toast.LENGTH_SHORT).show();
            return;
        }

        InputStream file = getInputStreamFromUri(fileUri);
        if (file == null) {
            Toast.makeText(this, "Unable To Process Content!", Toast.LENGTH_SHORT).show();
            return;
        }

        String fileName = getFileNameFromUri(fileUri);
        Log.d("processSharedFile", "fileName: " + fileName);

        String uploadUrl = savedUrl + "/api/upload";
        Log.d("processSharedFile", "uploadUrl: " + uploadUrl);
        String contentType = URLConnection.guessContentTypeFromName(fileName);
        Log.d("processSharedFile", "contentType: " + contentType);

        Toast.makeText(this, this.getString(R.string.tst_uploading_file), Toast.LENGTH_SHORT).show();

        new Thread(() -> {
            try {
                URL url = new URL(uploadUrl);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("POST");
                connection.setDoOutput(true);
                connection.setRequestProperty("Authorization", authToken);
                String boundary = "----WebKitFormBoundary7MA4YWxkTrZu0gW";
                connection.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);
                connection.connect();

                DataOutputStream outputStream = new DataOutputStream(connection.getOutputStream());

                // Write the boundary and the necessary headers
                outputStream.writeBytes("--" + boundary + "\r\n");
                outputStream.writeBytes("Content-Disposition: form-data; name=\"file\"; filename=\"" + fileName + "\"\r\n");
                outputStream.writeBytes("Content-Type: " + contentType + "\r\n");
                outputStream.writeBytes("Content-Transfer-Encoding: binary\r\n\r\n");

                // Write the file content
                byte[] buffer = new byte[4096];
                int bytesRead;
                while ((bytesRead = file.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, bytesRead);
                }
                file.close();

                // End the multipart request
                outputStream.writeBytes("\r\n--" + boundary + "--\r\n");
                outputStream.flush();
                outputStream.close();

                // Get the response code
                int responseCode = connection.getResponseCode();
                Log.d("processSharedFile", "responseCode: " + responseCode);
                String responseMessage = connection.getResponseMessage();
                Log.d("processSharedFile", "responseMessage: " + responseMessage);

                if (responseCode == HttpURLConnection.HTTP_OK) {
                    String jsonURL = parseJsonResponse(connection);
                    runOnUiThread(() -> copyToClipboard(jsonURL));
                } else {
                    runOnUiThread(() -> Toast.makeText(MainActivity.this, this.getString(R.string.tst_error) + ": " + responseMessage, Toast.LENGTH_SHORT).show());
                }
            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() -> Toast.makeText(MainActivity.this, this.getString(R.string.tst_error_uploading), Toast.LENGTH_SHORT).show());
            }
        }).start();
    }

    private InputStream getInputStreamFromUri(Uri uri) {
        try {
            return getContentResolver().openInputStream(uri);
        } catch (IOException e) {
            return null;
        }
    }

    private String getFileNameFromUri(Uri uri) {
        String fileName = null;
        try (Cursor cursor = getContentResolver().query(uri, null, null, null, null)) {
            if (cursor != null && cursor.moveToFirst()) {
                int nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                if (nameIndex != -1) {
                    fileName = cursor.getString(nameIndex);
                }
            }
        }
        return fileName;
    }

    private String parseJsonResponse(HttpURLConnection connection) {
        try {
            Log.d("parseJsonResponse", "Begin.");
            BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            StringBuilder response = new StringBuilder();
            String inputLine;
            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();

            Log.d("parseJsonResponse", "response: " + response);
            JSONObject jsonResponse = new JSONObject(response.toString());
            Log.d("parseJsonResponse", "JSONObject: " + jsonResponse);

            String name = jsonResponse.getString("name");
            String raw = jsonResponse.getString("raw");
            String url = jsonResponse.getString("url");

            Log.d("parseJsonResponse", "Name: " + name);
            Log.d("parseJsonResponse", "RAW: " + raw);
            Log.d("parseJsonResponse", "URL: " + url);

            return url;

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private void copyToClipboard(String url) {
        webView.loadUrl(url);
        ClipboardManager clipboard = (ClipboardManager) this.getSystemService(Context.CLIPBOARD_SERVICE);
        if (clipboard != null) {
            android.content.ClipData clip = android.content.ClipData.newPlainText("URL", url);
            clipboard.setPrimaryClip(clip);
            Toast.makeText(this, this.getString(R.string.tst_url_copied), Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, this.getString(R.string.tst_no_clipboard), Toast.LENGTH_SHORT).show();
        }
    }

    private class MyWebViewClient extends WebViewClient {
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
            String requestUrl = request.getUrl().toString();
            Log.d("shouldOverrideUrlLoading", "requestUrl: " + requestUrl);

            SharedPreferences preferences = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
            String savedUrl = preferences.getString(URL_KEY, null);
            Log.d("shouldOverrideUrlLoading", "savedUrl: " + savedUrl);

            if ((savedUrl != null &&
                    requestUrl.startsWith(savedUrl) &&
                    !requestUrl.startsWith(savedUrl + "/r/") &&
                    !requestUrl.startsWith(savedUrl + "/raw/")) ||
                    requestUrl.startsWith("https://discord.com/oauth2") ||
                    requestUrl.startsWith("https://github.com/sessions/two-factor/app") ||
                    requestUrl.startsWith("https://github.com/login") ||
                    requestUrl.startsWith("https://accounts.google.com/v3/signin") ||
                    requestUrl.startsWith("https://accounts.google.com/o/oauth2/v2/auth")) {
                Log.d("shouldOverrideUrlLoading", "FALSE - in app");
                return false;
            }

            Intent intent = new Intent(Intent.ACTION_VIEW, request.getUrl());
            startActivity(intent);
            Log.d("shouldOverrideUrlLoading", "TRUE - in browser");
            return true;
        }

        @Override
        public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError errorResponse) {
            Log.d("onReceivedError", "ERROR: " + errorResponse.getErrorCode());
            Toast.makeText(view.getContext(), "HTTP error: "+errorResponse.getDescription(), Toast.LENGTH_LONG).show();
            //showSettingsDialog();
        }

        @Override
        public void onReceivedHttpError(WebView view, WebResourceRequest request, WebResourceResponse errorResponse) {
            Log.d("onReceivedHttpError", "ERROR: " + errorResponse.getStatusCode());
            Toast.makeText(view.getContext(), "HTTP error: "+errorResponse.getReasonPhrase(), Toast.LENGTH_LONG).show();
            //showSettingsDialog();
        }
    }
}
