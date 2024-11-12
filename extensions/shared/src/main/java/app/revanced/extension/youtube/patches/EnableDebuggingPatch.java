package app.revanced.extension.youtube.patches;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import app.revanced.extension.shared.Logger;
import app.revanced.extension.shared.settings.BaseSettings;

@SuppressWarnings("unused")
public final class EnableDebuggingPatch {

    private static final ConcurrentMap<Long, Boolean> featureFlags
            = new ConcurrentHashMap<>(300, 0.75f, 1);

    /**
     * Injection point.
     */
    public static boolean isBooleanFeatureFlagEnabled(boolean value, long flag) {
        if (value && BaseSettings.DEBUG.get()) {
            if (featureFlags.putIfAbsent(flag, true) == null) {
                Logger.printDebug(() -> "boolean feature is enabled: " + flag);
            }
        }

        return value;
    }

    /**
     * Injection point.
     */
    public static double isDoubleFeatureFlagEnabled(double value, long flag, double defaultValue) {
        if (defaultValue != value && BaseSettings.DEBUG.get()) {
            if (featureFlags.putIfAbsent(flag, true) == null) {
                // Align the log outputs to make post processing easier.
                Logger.printDebug(() -> " double feature is enabled: " + flag
                        + " value: " + value + (defaultValue == 0 ? "" : " default: " + defaultValue));
            }
        }

        return value;
    }

    /**
     * Injection point.
     */
    public static long isLongFeatureFlagEnabled(long value, long flag, long defaultValue) {
        if (defaultValue != value && BaseSettings.DEBUG.get()) {
            if (featureFlags.putIfAbsent(flag, true) == null) {
                Logger.printDebug(() -> "   long feature is enabled: " + flag
                        + " value: " + value + (defaultValue == 0 ? "" : " default: " + defaultValue));
            }
        }

        return value;
    }

    /**
     * Injection point.
     */
    public static String isStringFeatureFlagEnabled(String value, long flag, String defaultValue) {
        if (BaseSettings.DEBUG.get() && !defaultValue.equals(value)) {
            if (featureFlags.putIfAbsent(flag, true) == null) {
                Logger.printDebug(() -> " string feature is enabled: " + flag
                        + " value: " + value +  (defaultValue.isEmpty() ? "" : " default: " + defaultValue));
            }
        }

        return value;
    }
}
