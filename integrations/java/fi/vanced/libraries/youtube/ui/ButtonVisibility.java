package fi.vanced.libraries.youtube.ui;

import android.content.Context;

import fi.vanced.utils.SharedPrefUtils;

public class ButtonVisibility {
    public static Visibility getButtonVisibility(Context context, String key) {
        return getButtonVisibility(context, key, "youtube");
    }

    public static Visibility getButtonVisibility(Context context, String key, String preferenceName) {
        String value = SharedPrefUtils.getString(context, preferenceName, key, null);

        if (value == null || value.isEmpty()) return Visibility.NONE;

        switch (value.toUpperCase()) {
            case "PLAYER": return Visibility.PLAYER;
            case "BUTTON_CONTAINER": return Visibility.BUTTON_CONTAINER;
            case "BOTH": return Visibility.BOTH;
            default: return Visibility.NONE;
        }
    }

    public static boolean isVisibleInContainer(Context context, String key) {
        return isVisibleInContainer(getButtonVisibility(context, key));
    }

    public static boolean isVisibleInContainer(Context context, String key, String preferenceName) {
        return isVisibleInContainer(getButtonVisibility(context, key, preferenceName));
    }

    public static boolean isVisibleInContainer(Visibility visibility) {
        return visibility == Visibility.BOTH || visibility == Visibility.BUTTON_CONTAINER;
    }
}
