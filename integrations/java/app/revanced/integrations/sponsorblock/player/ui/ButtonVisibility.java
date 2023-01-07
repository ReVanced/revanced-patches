package app.revanced.integrations.sponsorblock.player.ui;

import android.content.Context;

import app.revanced.integrations.utils.SharedPrefHelper;

public class ButtonVisibility {
    public static Visibility getButtonVisibility(String key) {
        return getButtonVisibility(key, SharedPrefHelper.SharedPrefNames.YOUTUBE);
    }

    public static Visibility getButtonVisibility(String key, SharedPrefHelper.SharedPrefNames name) {
        String value = SharedPrefHelper.getString(name, key, null);

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

    public static boolean isVisibleInContainer(String key) {
        return isVisibleInContainer(getButtonVisibility(key));
    }

    public static boolean isVisibleInContainer(String key, SharedPrefHelper.SharedPrefNames name) {
        return isVisibleInContainer(getButtonVisibility(key, name));
    }

    public static boolean isVisibleInContainer(Visibility visibility) {
        return visibility == Visibility.BOTH || visibility == Visibility.BUTTON_CONTAINER;
    }
}
