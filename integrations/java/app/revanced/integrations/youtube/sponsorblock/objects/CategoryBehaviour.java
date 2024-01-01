package app.revanced.integrations.youtube.sponsorblock.objects;

import static app.revanced.integrations.shared.StringRef.sf;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.Objects;

import app.revanced.integrations.shared.Utils;
import app.revanced.integrations.shared.StringRef;

public enum CategoryBehaviour {
    SKIP_AUTOMATICALLY("skip", 2, true, sf("sb_skip_automatically")),
    // desktop does not have skip-once behavior. Key is unique to ReVanced
    SKIP_AUTOMATICALLY_ONCE("skip-once", 3, true, sf("sb_skip_automatically_once")),
    MANUAL_SKIP("manual-skip", 1, false, sf("sb_skip_showbutton")),
    SHOW_IN_SEEKBAR("seekbar-only", 0, false, sf("sb_skip_seekbaronly")),
    // ignored categories are not exported to json, and ignore is the default behavior when importing
    IGNORE("ignore", -1, false, sf("sb_skip_ignore"));

    /**
     * ReVanced specific value.
     */
    @NonNull
    public final String reVancedKeyValue;
    /**
     * Desktop specific value.
     */
    public final int desktopKeyValue;
    /**
     * If the segment should skip automatically
     */
    public final boolean skipAutomatically;
    @NonNull
    public final StringRef description;

    CategoryBehaviour(String reVancedKeyValue, int desktopKeyValue, boolean skipAutomatically, StringRef description) {
        this.reVancedKeyValue = Objects.requireNonNull(reVancedKeyValue);
        this.desktopKeyValue = desktopKeyValue;
        this.skipAutomatically = skipAutomatically;
        this.description = Objects.requireNonNull(description);
    }

    @Nullable
    public static CategoryBehaviour byReVancedKeyValue(@NonNull String keyValue) {
        for (CategoryBehaviour behaviour : values()){
            if (behaviour.reVancedKeyValue.equals(keyValue)) {
                return behaviour;
            }
        }
        return null;
    }

    @Nullable
    public static CategoryBehaviour byDesktopKeyValue(int desktopKeyValue) {
        for (CategoryBehaviour behaviour : values()) {
            if (behaviour.desktopKeyValue == desktopKeyValue) {
                return behaviour;
            }
        }
        return null;
    }

    private static String[] behaviorKeyValues;
    private static String[] behaviorDescriptions;

    private static String[] behaviorKeyValuesWithoutSkipOnce;
    private static String[] behaviorDescriptionsWithoutSkipOnce;

    private static void createNameAndKeyArrays() {
        Utils.verifyOnMainThread();

        CategoryBehaviour[] behaviours = values();
        final int behaviorLength = behaviours.length;
        behaviorKeyValues = new String[behaviorLength];
        behaviorDescriptions = new String[behaviorLength];
        behaviorKeyValuesWithoutSkipOnce = new String[behaviorLength - 1];
        behaviorDescriptionsWithoutSkipOnce = new String[behaviorLength - 1];

        int behaviorIndex = 0, behaviorHighlightIndex = 0;
        while (behaviorIndex < behaviorLength) {
            CategoryBehaviour behaviour = behaviours[behaviorIndex];
            String value = behaviour.reVancedKeyValue;
            String description = behaviour.description.toString();
            behaviorKeyValues[behaviorIndex] = value;
            behaviorDescriptions[behaviorIndex] = description;
            behaviorIndex++;
            if (behaviour != SKIP_AUTOMATICALLY_ONCE) {
                behaviorKeyValuesWithoutSkipOnce[behaviorHighlightIndex] = value;
                behaviorDescriptionsWithoutSkipOnce[behaviorHighlightIndex] = description;
                behaviorHighlightIndex++;
            }
        }
    }

    static String[] getBehaviorKeyValues() {
        if (behaviorKeyValues == null) {
            createNameAndKeyArrays();
        }
        return behaviorKeyValues;
    }
    static String[] getBehaviorKeyValuesWithoutSkipOnce() {
        if (behaviorKeyValuesWithoutSkipOnce == null) {
            createNameAndKeyArrays();
        }
        return behaviorKeyValuesWithoutSkipOnce;
    }

    static String[] getBehaviorDescriptions() {
        if (behaviorDescriptions == null) {
            createNameAndKeyArrays();
        }
        return behaviorDescriptions;
    }
    static String[] getBehaviorDescriptionsWithoutSkipOnce() {
        if (behaviorDescriptionsWithoutSkipOnce == null) {
            createNameAndKeyArrays();
        }
        return behaviorDescriptionsWithoutSkipOnce;
    }
}
