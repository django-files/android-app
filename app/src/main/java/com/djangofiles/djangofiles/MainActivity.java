package com.djangofiles.djangofiles;

import android.Manifest;
import android.annotation.SuppressLint;
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

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class MainActivity extends AppCompatActivity {

    private static final String LOG_TAG = "com.djangofiles.djangofiles";
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
        webView.addJavascriptInterface(new WebAppInterface(this), "Android");
        webView.setWebViewClient(new MyWebViewClient());

        // Handle Intent
        handleIntent(getIntent());

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        SharedPreferences preferences = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String savedUrl = preferences.getString(URL_KEY, null);
        Log.d(LOG_TAG, "savedUrl: " + savedUrl);
        String authToken = preferences.getString(TOKEN_KEY, null);
        Log.d(LOG_TAG, "authToken: " + authToken);

        if (savedUrl == null || savedUrl.isEmpty()) {
            showUrlInputDialog(preferences);
        } else {
            webView.loadUrl(savedUrl);
        }

        // Request Permissions at Start for Now...
        requestPermissions();

        // webView.evaluateJavascript("getAuthToken();", null);
        // Log.d(LOG_TAG, "document.getElementById('auth-token').value");
        // webView.evaluateJavascript("getAuthToken();", null);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        handleIntent(intent);
    }

    private void handleIntent(Intent intent) {
        if (Intent.ACTION_VIEW.equals(intent.getAction())) {
            // Handle open with
            Uri fileUri = intent.getData();
            if (fileUri != null) {
                processSharedFile(fileUri); // Implement your file handling logic
            }
        } else if (Intent.ACTION_SEND.equals(intent.getAction()) && intent.getType() != null) {
            if ("text/plain".equals(intent.getType())) {
                // Handle text sharing
                Toast.makeText(this, "Not Implemented.", Toast.LENGTH_SHORT).show();
            } else {
                // Handle file sharing
                Uri fileUri = intent.getParcelableExtra(Intent.EXTRA_STREAM);
                if (fileUri != null) {
                    processSharedFile(fileUri);
                }
            }
        } else if (Intent.ACTION_SEND_MULTIPLE.equals(intent.getAction())) {
            // Handle multiple file sharing
            ArrayList<Uri> fileUris = intent.getParcelableArrayListExtra(Intent.EXTRA_STREAM);
            if (fileUris != null) {
                for (Uri fileUri : fileUris) {
                    processSharedFile(fileUri);
                }
            }
        }
    }

    private void processSharedFile(Uri fileUri) {
        SharedPreferences preferences = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String savedUrl = preferences.getString(URL_KEY, null);
        Log.d(LOG_TAG, "savedUrl: " + savedUrl);
        String authToken = preferences.getString(TOKEN_KEY, null);
        Log.d(LOG_TAG, "authToken: " + authToken);
        if (savedUrl == null) {
            Toast.makeText(this, "Saved URL is not found", Toast.LENGTH_SHORT).show();
            return;
        }

        String uploadUrl = savedUrl + "/api/upload";
        File file = new File(getRealPathFromURI(fileUri));
        Log.d(LOG_TAG, "uploadUrl:" + uploadUrl);
        Log.d(LOG_TAG, "file.name:" + file.getName());
        Log.d(LOG_TAG, "Content-Type:" + URLConnection.guessContentTypeFromName(file.getName()));

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
                String responseMessage = connection.getResponseMessage();
                Log.d(LOG_TAG, "responseCode: " + responseCode);
                Log.d(LOG_TAG, "responseMessage: " + responseMessage);

                if (responseCode == HttpURLConnection.HTTP_OK) {
                    runOnUiThread(() -> Toast.makeText(MainActivity.this, "File Uploaded", Toast.LENGTH_SHORT).show());
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
                // This currently happens on startup so nothing happens here...
            } else {
                Toast.makeText(this, "Permission denied. Cannot access file.", Toast.LENGTH_SHORT).show();
            }
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
