package app.revanced.extension.shared.patches;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import app.revanced.extension.shared.Logger;
import app.revanced.extension.shared.settings.BaseSettings;

@SuppressWarnings("unused")
public final class EnableDebuggingPatch {

    /**
     * Only log if debugging is enabled on startup.
     * This prevents enabling debugging
     * while the app is running then failing to restart
     * resulting in an incomplete log.
     */
    private static final boolean LOG_FEATURE_FLAGS = BaseSettings.DEBUG.get();

    private static final ConcurrentMap<Long, Boolean> featureFlags = LOG_FEATURE_FLAGS
            ? new ConcurrentHashMap<>(800, 0.5f, 1)
            : null;

    private static final Set<Long> DISABLED_FEATURE_FLAGS = parseFlags(BaseSettings.DISABLED_FEATURE_FLAGS.get());
    private static final Set<Long> ENABLED_FEATURE_FLAGS = parseFlags(BaseSettings.ENABLED_FEATURE_FLAGS.get());

    // Parse the string of flags.
    private static Set<Long> parseFlags(String flags) {
        Set<Long> parsedFlags = new HashSet<>();
        if (flags != null && !flags.trim().isEmpty()) {
            for (String flag : flags.split("\n")) {
                String trimmedFlag = flag.trim();
                try {
                    parsedFlags.add(Long.parseLong(trimmedFlag));
                } catch (NumberFormatException e) {
                    Logger.printDebug(() -> "Invalid flag ID: " + flag);
                }
            }
        }
        return parsedFlags;
    }

    /**
     * Injection point.
     */
    public static boolean isBooleanFeatureFlagEnabled(boolean value, Long flag) {
        if (ENABLED_FEATURE_FLAGS.contains(flag)) {
            Logger.printDebug(() -> "Flag enabled: " + flag);
            return true;
        }
        if (DISABLED_FEATURE_FLAGS.contains(flag)) {
            Logger.printDebug(() -> "Flag disabled: " + flag);
            return false;
        }
        if (LOG_FEATURE_FLAGS && value && featureFlags.putIfAbsent(flag, true) == null) {
            Logger.printDebug(() -> "boolean feature is enabled: " + flag);
        }

        return value;
    }

    /**
     * Injection point.
     */
    public static double isDoubleFeatureFlagEnabled(double value, long flag, double defaultValue) {
        if (LOG_FEATURE_FLAGS && defaultValue != value) {
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
        if (LOG_FEATURE_FLAGS && defaultValue != value) {
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
        if (LOG_FEATURE_FLAGS && !defaultValue.equals(value)) {
            if (featureFlags.putIfAbsent(flag, true) == null) {
                Logger.printDebug(() -> " string feature is enabled: " + flag
                        + " value: " + value +  (defaultValue.isEmpty() ? "" : " default: " + defaultValue));
            }
        }

        return value;
    }
}
