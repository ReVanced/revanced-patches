package app.revanced.extension.gamehub.steam;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import app.revanced.extension.gamehub.prefs.GameHubPrefs;

/**
 * Receives broadcast commands from external apps (e.g. EmuReady) to control the Steam
 * storage path used by the GameHub SD-card-storage patch.
 *
 * Action suffixes (appended to the app's own package name):
 *   .GET_STEAM_STORAGE  — no-op
 *   .SET_STEAM_STORAGE  — extra "path" (String) sets the custom storage path
 *   .USE_INTERNAL_STORAGE — disables custom storage, reverts to default
 *
 * Example for package com.xiaoji.egggame:
 *   am broadcast -a com.xiaoji.egggame.SET_STEAM_STORAGE --es path /sdcard/GHL
 */
@SuppressWarnings("unused")
public class StorageBroadcastReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context ctx, Intent intent) {
        if (intent == null || ctx == null) return;
        String pkg = ctx.getPackageName();
        String action = intent.getAction();
        if ((pkg + ".SET_STEAM_STORAGE").equals(action)) {
            String path = intent.getStringExtra("path");
            if (path != null && !path.isEmpty()) {
                GameHubPrefs.setStoragePath(path);
                // Also enable custom storage so the new path takes effect immediately.
                if (!GameHubPrefs.isCustomStorageEnabled()) {
                    GameHubPrefs.toggleStorageLocation();
                }
            }
        } else if ((pkg + ".USE_INTERNAL_STORAGE").equals(action)) {
            GameHubPrefs.useInternalStorage();
        }
        // GET_STEAM_STORAGE: no-op.
    }
}
