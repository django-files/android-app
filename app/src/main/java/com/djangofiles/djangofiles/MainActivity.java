package com.djangofiles.djangofiles;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.Settings;
import android.util.Log;
import android.view.KeyEvent;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.EditText;
import android.widget.Toast;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Objects;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;


public class MainActivity extends AppCompatActivity {

    private static final String PREFS_NAME = "AppPreferences";
    private static final String URL_KEY = "saved_url";
    private static final String TOKEN_KEY = "auth_token";
    private static final int REQUEST_CODE_READ_EXTERNAL_STORAGE = 1;

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
        webView.getSettings().setUserAgentString("AndroidDjangoFiles");
        webView.addJavascriptInterface(new WebAppInterface(this), "Android");
        webView.setWebViewClient(new MyWebViewClient());

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        Log.d("onCreate", "----- onCreate -----");
        Log.d("onCreate", "getAction: " + getIntent().getAction());
        Log.d("onCreate", "getData: " + getIntent().getData());
        Log.d("onCreate", "getExtras: " + getIntent().getExtras());

        // Handle Intent
        handleIntent(getIntent());

        // Request Permissions at Start for Now...
        requestPermissions();

        // // Token is handled in WebAppInterface via the websites main.js
        // webView.evaluateJavascript("getAuthToken();", null);
        // Log.d("onCreate", "document.getElementById('auth-token').value");
        // webView.evaluateJavascript("getAuthToken();", null);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        Log.d("onNewIntent", "intent: " + intent);
        handleIntent(intent);
    }

    private void handleIntent(Intent intent) {
        // TODO: Need to do some serious debugging on intent handling...
        if (Intent.ACTION_MAIN.equals(intent.getAction())) {
            Log.d("handleIntent", "ACTION_MAIN");
            SharedPreferences preferences = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
            String savedUrl = preferences.getString(URL_KEY, null);
            Log.d("onCreate", "savedUrl: " + savedUrl);
            String authToken = preferences.getString(TOKEN_KEY, null);
            Log.d("onCreate", "authToken: " + authToken);

            String currentUrl = webView.getUrl();
            Log.d("onCreate", "currentUrl: " + currentUrl);

            if (savedUrl == null || savedUrl.isEmpty()) {
                showSettingsDialog();
            } else {
                if (currentUrl == null) {
                    Log.d("onCreate", "webView.loadUrl");
                    webView.loadUrl(savedUrl);
                } else {
                    Log.d("onCreate", "SKIPPING  webView.loadUrl");
                }
            }
        } else if (Intent.ACTION_VIEW.equals(intent.getAction())) {
            Log.d("handleIntent", "ACTION_VIEW");
            Uri uri = intent.getData();
            Log.d("handleIntent", "uri: " + uri);
            if (uri != null) {
                if ("djangofiles".equals(uri.getScheme())) {
                    Log.d("handleIntent", "Deep Link: " + uri);
                    if ("serverlist".equals(uri.getHost())) {
                        Log.d("handleIntent", "showSettingsDialog");
                        showSettingsDialog();
                    } else {
                        Log.d("handleIntent", "Unknown DeepLink!");
                    }
                } else {
                    Log.d("handleIntent", "processSharedFile: " + uri);
                    processSharedFile(uri);
                }
            } else {
                Log.e("IntentDebug", "Unknown Intent!");
                //showSettingsDialog();
            }
        } else if (Intent.ACTION_SEND.equals(intent.getAction()) && intent.getType() != null) {
            Log.d("handleIntent", "ACTION_SEND");
            if ("text/plain".equals(intent.getType())) {
                Toast.makeText(this, "Not Implemented.", Toast.LENGTH_SHORT).show();
            } else {
                Uri fileUri = intent.getParcelableExtra(Intent.EXTRA_STREAM);
                if (fileUri != null) {
                    processSharedFile(fileUri);
                }
            }
        } else if (Intent.ACTION_SEND_MULTIPLE.equals(intent.getAction())) {
            Log.d("handleIntent", "ACTION_SEND_MULTIPLE");
            ArrayList<Uri> fileUris = intent.getParcelableArrayListExtra(Intent.EXTRA_STREAM);
            if (fileUris != null) {
                for (Uri fileUri : fileUris) {
                    processSharedFile(fileUri);
                }
            }
        }
    }

    public void showSettingsDialog() {
        SharedPreferences preferences = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String savedUrl = preferences.getString(URL_KEY, null);
        Log.d("showSettingsDialog", "savedUrl: " + savedUrl);

        EditText input = new EditText(this);
        input.setHint("Example: df.cssnr.com");
        if (savedUrl != null) {
            input.setText(savedUrl);
        }

        runOnUiThread(() -> {
            new AlertDialog.Builder(this)
                    .setTitle("App Settings")
                    .setView(input)
                    .setNegativeButton("Exit", (dialog, which) -> finish())
                    .setPositiveButton("OK", (dialog, which) -> {
                        String url = input.getText().toString().trim();
                        Log.d("showSettingsDialog", "setPositiveButton: url:" + url);
                        if (url.isEmpty()) {
                            // TODO: Need to add verification here and keep dialog open...
                            Toast.makeText(this, "Please enter a valid URL", Toast.LENGTH_SHORT).show();
                            finish();
                            return;
                        }
                        if (!url.startsWith("http://") && !url.startsWith("https://")) {
                            url = "https://" + url;
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

    // private void showUrlInputDialog(SharedPreferences preferences) {
    //     EditText input = new EditText(this);
    //     input.setHint("Example: df.cssnr.com");
    //
    //     new AlertDialog.Builder(this)
    //             .setTitle("Your Django Files URL")
    //             .setView(input)
    //             .setCancelable(false)
    //             .setPositiveButton("Save", (dialog, which) -> {
    //                 String url = input.getText().toString().trim();
    //                 if (!url.startsWith("http://") && !url.startsWith("https://")) {
    //                     url = "https://" + url;
    //                 }
    //                 preferences.edit().putString(URL_KEY, url).apply();
    //                 webView.loadUrl(url);
    //             })
    //             .setNegativeButton("Exit", (dialog, which) -> finish())
    //             .show();
    // }

    private void processSharedFile(Uri fileUri) {
        SharedPreferences preferences = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String savedUrl = preferences.getString(URL_KEY, null);
        Log.d("processSharedFile", "savedUrl: " + savedUrl);
        String authToken = preferences.getString(TOKEN_KEY, null);
        Log.d("processSharedFile", "authToken: " + authToken);
        if (savedUrl == null) {
            Toast.makeText(this, "Saved URL is not found", Toast.LENGTH_SHORT).show();
            return;
        }

        String uploadUrl = savedUrl + "/api/upload";
        File file = new File(getRealPathFromURI(fileUri));
        Log.d("processSharedFile", "uploadUrl:" + uploadUrl);
        Log.d("processSharedFile", "file.name:" + file.getName());
        Log.d("processSharedFile", "Content-Type:" + URLConnection.guessContentTypeFromName(file.getName()));

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
                outputStream.writeBytes("Content-Disposition: form-data; name=\"file\"; filename=\"" + file.getName() + "\"\r\n");
                outputStream.writeBytes("Content-Type: " + URLConnection.guessContentTypeFromName(file.getName()) + "\r\n");
                outputStream.writeBytes("Content-Transfer-Encoding: binary\r\n\r\n");

                // Write the file content
                FileInputStream fileInputStream = new FileInputStream(file);
                byte[] buffer = new byte[4096];
                int bytesRead;
                while ((bytesRead = fileInputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, bytesRead);
                }
                fileInputStream.close();

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
                    runOnUiThread(() -> Toast.makeText(MainActivity.this, "Error: " + responseMessage, Toast.LENGTH_SHORT).show());
                }
            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() -> Toast.makeText(MainActivity.this, "Error Uploading!", Toast.LENGTH_SHORT).show());
            }
        }).start();
    }

    private String getRealPathFromURI(Uri contentUri) {
        String[] proj = {MediaStore.Images.Media.DATA};
        Cursor cursor = getContentResolver().query(contentUri, proj, null, null, null);
        if (cursor != null) {
            cursor.moveToFirst();
            int columnIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            String filePath = cursor.getString(columnIndex);
            cursor.close();
            return filePath;
        }
        return null;
    }

    private void requestPermissions() {
        // Check and request READ_EXTERNAL_STORAGE permission
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                    REQUEST_CODE_READ_EXTERNAL_STORAGE);
        }
        // Check if the app has MANAGE_EXTERNAL_STORAGE permission (for Android 11+)
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
            if (!Environment.isExternalStorageManager()) {
                Intent intent = new Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);
            }
        }
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
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE_READ_EXTERNAL_STORAGE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // This currently happens on startup so nothing happens here yet...
            } else {
                Toast.makeText(this, "Permission denied. Cannot access file.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    public String parseJsonResponse(HttpURLConnection connection) {
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

    public void copyToClipboard(String url) {
        ClipboardManager clipboard = (ClipboardManager) MainActivity.this.getSystemService(Context.CLIPBOARD_SERVICE);
        if (clipboard != null) {
            android.content.ClipData clip = android.content.ClipData.newPlainText("URL", url);
            clipboard.setPrimaryClip(clip);
            Toast.makeText(MainActivity.this, "URL copied to clipboard", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(MainActivity.this, "Clipboard not available", Toast.LENGTH_SHORT).show();
        }
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
