package app.revanced.extension.shared.patches;

import static java.lang.Boolean.TRUE;

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

    // Log all disabled flags on app startup.
    static {
        if (LOG_FEATURE_FLAGS && !DISABLED_FEATURE_FLAGS.isEmpty()) {
            StringBuilder sb = new StringBuilder("Disabled feature flags:\n");
            for (Long flag : DISABLED_FEATURE_FLAGS) {
                sb.append("  ").append(flag).append('\n');
            }
            Logger.printDebug(sb::toString);
        }
    }

    /**
     * Injection point.
     */
    public static boolean isBooleanFeatureFlagEnabled(boolean value, Long flag) {
        if (LOG_FEATURE_FLAGS && value) {
            if (DISABLED_FEATURE_FLAGS.contains(flag)) {
                return false;
            }
            if (featureFlags.putIfAbsent(flag, TRUE) == null) {
                Logger.printDebug(() -> "boolean feature is enabled: " + flag);
            }
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
                        + " value: " + value + (defaultValue.isEmpty() ? "" : " default: " + defaultValue));
            }
        }

        return value;
    }

    /**
     * Get all logged feature flags.
     * @return Set of all known flags
     */
    public static Set<Long> getAllLoggedFlags() {
        if (featureFlags != null) {
            return new HashSet<>(featureFlags.keySet());
        }

        return new HashSet<>();
    }

    /**
     * Public method for parsing flags.
     * @param flags String containing newline-separated flag IDs
     * @return Set of parsed flag IDs
     */
    public static Set<Long> parseFlags(String flags) {
        Set<Long> parsedFlags = new HashSet<>();
        if (!flags.isBlank()) {
            for (String flag : flags.split("\n")) {
                String trimmedFlag = flag.trim();
                if (trimmedFlag.isEmpty()) continue; // Skip empty lines.
                try {
                    parsedFlags.add(Long.parseLong(trimmedFlag));
                } catch (NumberFormatException e) {
                    Logger.printException(() -> "Invalid flag ID: " + flag);
                }
            }
        }

        return parsedFlags;
    }
}
