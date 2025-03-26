package com.djangofiles.djangofiles;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import android.webkit.JavascriptInterface;
import android.widget.Toast;

public class WebAppInterface {

    private static final String PREFS_NAME = "AppPreferences";
    private static final String TOKEN_KEY = "auth_token";

    Context mContext;

    /**
     * Instantiate the interface and set the context.
     */
    WebAppInterface(Context c) {
        mContext = c;
    }

    /**
     * Show a toast from the web page.
     */
    @JavascriptInterface
    public void showToast(String toast) {
        Toast.makeText(mContext, toast, Toast.LENGTH_SHORT).show();
    }

    /**
     * Receive auth token from web page.
     */
    @JavascriptInterface
    public void receiveAuthToken(String authToken) {
        Log.d("receiveAuthToken", "Received auth token: " + authToken);
        Log.d("receiveAuthToken", "PREFS_NAME: " + PREFS_NAME);
        SharedPreferences preferences = mContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        Log.d("receiveAuthToken", "TOKEN_KEY: " + TOKEN_KEY);
        preferences.edit().putString(TOKEN_KEY, authToken).apply();
        // SharedPreferences.Editor editor = preferences.edit();
        // editor.putString(TOKEN_KEY, authToken);
        // editor.apply();
        Log.d("receiveAuthToken", "Auth Token Saved.");
    }

    // @JavascriptInterface
    // public void showSettingsDialog() {
    //     if (mContext instanceof MainActivity) {
    //         ((MainActivity) mContext).runOnUiThread(() -> {
    //             new android.app.AlertDialog.Builder(mContext)
    //                     .setTitle("App Settings")
    //                     .setMessage("This is where your settings would be displayed.")
    //                     .setPositiveButton("OK", (dialog, which) -> dialog.dismiss())
    //                     .show();
    //         });
    //     }
    // }

}
