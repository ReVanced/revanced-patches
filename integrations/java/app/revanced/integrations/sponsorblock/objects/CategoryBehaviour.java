package app.revanced.integrations.sponsorblock.objects;

import static app.revanced.integrations.utils.StringRef.sf;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.Objects;

import app.revanced.integrations.utils.ReVancedUtils;
import app.revanced.integrations.utils.StringRef;

public enum CategoryBehaviour {
    SKIP_AUTOMATICALLY("skip", 2, true, sf("sb_skip_automatically")),
    // desktop does not have skip-once behavior. Key is unique to ReVanced
    SKIP_AUTOMATICALLY_ONCE("skip-once", 3, true, sf("sb_skip_automatically_once")),
    MANUAL_SKIP("manual-skip", 1, false, sf("sb_skip_showbutton")),
    SHOW_IN_SEEKBAR("seekbar-only", 0, false, sf("sb_skip_seekbaronly")),
    // ignored categories are not exported to json, and ignore is the default behavior when importing
    IGNORE("ignore", -1, false, sf("sb_skip_ignore"));

    @NonNull
    public final String key;
    public final int desktopKey;
    /**
     * If the segment should skip automatically
     */
    public final boolean skipAutomatically;
    @NonNull
    public final StringRef description;

    CategoryBehaviour(String key, int desktopKey, boolean skipAutomatically, StringRef description) {
        this.key = Objects.requireNonNull(key);
        this.desktopKey = desktopKey;
        this.skipAutomatically = skipAutomatically;
        this.description = Objects.requireNonNull(description);
    }

    @Nullable
    public static CategoryBehaviour byStringKey(@NonNull String key) {
        for (CategoryBehaviour behaviour : values()){
            if (behaviour.key.equals(key)) {
                return behaviour;
            }
        }
        return null;
    }

    @Nullable
    public static CategoryBehaviour byDesktopKey(int desktopKey) {
        for (CategoryBehaviour behaviour : values()) {
            if (behaviour.desktopKey == desktopKey) {
                return behaviour;
            }
        }
        return null;
    }

    private static String[] behaviorKeys;
    private static String[] behaviorDescriptions;

    private static String[] behaviorKeysWithoutSkipOnce;
    private static String[] behaviorDescriptionsWithoutSkipOnce;

    private static void createNameAndKeyArrays() {
        ReVancedUtils.verifyOnMainThread();

        CategoryBehaviour[] behaviours = values();
        final int behaviorLength = behaviours.length;
        behaviorKeys = new String[behaviorLength];
        behaviorDescriptions = new String[behaviorLength];
        behaviorKeysWithoutSkipOnce = new String[behaviorLength - 1];
        behaviorDescriptionsWithoutSkipOnce = new String[behaviorLength - 1];

        int behaviorIndex = 0, behaviorHighlightIndex = 0;
        while (behaviorIndex < behaviorLength) {
            CategoryBehaviour behaviour = behaviours[behaviorIndex];
            String key = behaviour.key;
            String description = behaviour.description.toString();
            behaviorKeys[behaviorIndex] = key;
            behaviorDescriptions[behaviorIndex] = description;
            behaviorIndex++;
            if (behaviour != SKIP_AUTOMATICALLY_ONCE) {
                behaviorKeysWithoutSkipOnce[behaviorHighlightIndex] = key;
                behaviorDescriptionsWithoutSkipOnce[behaviorHighlightIndex] = description;
                behaviorHighlightIndex++;
            }
        }
    }

    static String[] getBehaviorKeys() {
        if (behaviorKeys == null) {
            createNameAndKeyArrays();
        }
        return behaviorKeys;
    }
    static String[] getBehaviorKeysWithoutSkipOnce() {
        if (behaviorKeysWithoutSkipOnce == null) {
            createNameAndKeyArrays();
        }
        return behaviorKeysWithoutSkipOnce;
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
