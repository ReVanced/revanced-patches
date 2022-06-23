package app.revanced.integrations.sponsorblock.player.ui;

import android.content.Context;

import app.revanced.integrations.utils.SharedPrefHelper;

public class ButtonVisibility {
    public static Visibility getButtonVisibility(Context context, String key) {
        return getButtonVisibility(context, key, SharedPrefHelper.SharedPrefNames.YOUTUBE);
    }

    public static Visibility getButtonVisibility(Context context, String key, SharedPrefHelper.SharedPrefNames name) {
        String value = SharedPrefHelper.getString(context, name, key, null);

        if (value == null || value.isEmpty()) return Visibility.NONE;

        switch (value.toUpperCase()) {
            case "PLAYER":
                return Visibility.PLAYER;
            case "BUTTON_CONTAINER":
                return Visibility.BUTTON_CONTAINER;
            case "BOTH":
                return Visibility.BOTH;
            default:
                return Visibility.NONE;
        }
    }

    public static boolean isVisibleInContainer(Context context, String key) {
        return isVisibleInContainer(getButtonVisibility(context, key));
    }

    public static boolean isVisibleInContainer(Context context, String key, SharedPrefHelper.SharedPrefNames name) {
        return isVisibleInContainer(getButtonVisibility(context, key, name));
    }

    public static boolean isVisibleInContainer(Visibility visibility) {
        return visibility == Visibility.BOTH || visibility == Visibility.BUTTON_CONTAINER;
    }
}
