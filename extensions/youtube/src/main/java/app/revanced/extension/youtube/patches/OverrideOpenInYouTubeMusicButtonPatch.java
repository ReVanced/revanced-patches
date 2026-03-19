package app.revanced.extension.youtube.patches;


import android.content.Intent;
import android.net.Uri;

import androidx.annotation.Nullable;

import app.revanced.extension.shared.Utils;
import app.revanced.extension.youtube.settings.Settings;

@SuppressWarnings("unused")
public class OverrideOpenInYouTubeMusicButtonPatch {

    private static final String YOUTUBE_MUSIC_PACKAGE_NAME = "com.google.android.apps.youtube.music";

    private static final Boolean overrideButton = Settings.OVERRIDE_OPEN_IN_YOUTUBE_MUSIC_BUTTON.get();

    private static final String overridePackageName = getOverridePackageName();

    @SuppressWarnings("SameReturnValue")
    public static String getOverridePackageName() {
        return ""; // Value is replaced during patching.
    }

    public static @Nullable Intent overrideSetPackage(@Nullable Intent intent, @Nullable String packageName) {
        if (intent == null || !overrideButton) return intent;

        if (YOUTUBE_MUSIC_PACKAGE_NAME.equals(packageName)) {
            if (Utils.isNotEmpty(overridePackageName) && Utils.isPackageEnabled(overridePackageName)) {
                return intent.setPackage(overridePackageName);
            }

            return intent.setPackage(null);
        }

        return intent.setPackage(packageName);
    }

    public static @Nullable Intent overrideSetData(@Nullable Intent intent, @Nullable Uri uri) {
        if (intent == null || uri == null || !overrideButton) return intent;

        String uriString = uri.toString();
        if (uriString.contains(YOUTUBE_MUSIC_PACKAGE_NAME)) {
            if ("market".equals(uri.getScheme()) || uriString.contains("play.google.com/store/apps")) {
                intent.setData(Uri.parse("https://music.youtube.com/"));

                if (Utils.isNotEmpty(overridePackageName) && Utils.isPackageEnabled(overridePackageName)) {
                    intent.setPackage(overridePackageName);
                } else {
                    intent.setPackage(null);
                }

                return intent;
            }
        }

        return intent.setData(uri);
    }
}
