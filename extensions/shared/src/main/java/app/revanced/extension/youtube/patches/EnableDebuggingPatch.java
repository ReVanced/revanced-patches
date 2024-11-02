package app.revanced.extension.youtube.patches;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import app.revanced.extension.shared.Logger;
import app.revanced.extension.shared.settings.BaseSettings;

@SuppressWarnings("unused")
public final class EnableDebuggingPatch {

    private static final ConcurrentMap<Long, Boolean> featureFlags
            = new ConcurrentHashMap<>(100, 0.75f, 1);

    public static boolean isFeatureFlagEnabled(long flag, boolean value) {
        if (value && BaseSettings.DEBUG.get()) {
            if (featureFlags.putIfAbsent(flag, true) == null) {
                Logger.printDebug(() -> "feature is enabled: " + flag);
            }
        }

        return value;
    }
}
