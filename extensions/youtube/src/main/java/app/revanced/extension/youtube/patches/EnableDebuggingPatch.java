package app.revanced.extension.youtube.patches;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.widget.Toast;
import app.revanced.extension.shared.Logger;
import app.revanced.extension.shared.Utils;
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
    private static final int MAX_LOG_LINES = 1000;
    private static final StringBuilder logBuffer = LOG_FEATURE_FLAGS ? new StringBuilder() : null;

    /**
     * Appends a log message to the internal buffer.
     * Limits the buffer to the last 1000 lines to prevent excessive memory usage.
     *
     * @param message The log message to append.
     */
    private static void appendToLogBuffer(String message) {
        if (LOG_FEATURE_FLAGS && logBuffer != null) {
            synchronized (logBuffer) {
                logBuffer.append(message).append("\n");
                // Limit buffer to last 1000 lines
                int lineCount = logBuffer.toString().split("\n").length;
                if (lineCount > MAX_LOG_LINES) {
                    String[] lines = logBuffer.toString().split("\n");
                    logBuffer.setLength(0);
                    for (int i = Math.max(0, lines.length - MAX_LOG_LINES); i < lines.length; i++) {
                        logBuffer.append(lines[i]).append("\n");
                    }
                }
            }
        }
    }

    /**
     * Exports all logs from the internal buffer to the clipboard.
     * Clears the buffer after export to free memory.
     *
     * @param context The Android context for accessing the clipboard and showing toasts.
     */
    public static void exportLogcatToClipboard(Context context) {
        if (!LOG_FEATURE_FLAGS || logBuffer == null) {
            Utils.showToastLong("Debug logging is disabled!");
            return;
        }
        try {
            StringBuilder exportedLogs = new StringBuilder();
            synchronized (logBuffer) {
                exportedLogs.append(logBuffer.toString());
                logBuffer.setLength(0); // Clear buffer after export
            }
            if (exportedLogs.length() > 0) {
                ClipboardManager clipboard = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText("ReVanced Logs", exportedLogs.toString());
                clipboard.setPrimaryClip(clip);
                Logger.printDebug(() -> "Logs copied to clipboard.");
                appendToLogBuffer("Logs copied to clipboard.");
                Utils.showToastLong("Logs copied to clipboard!");
            } else {
                Logger.printDebug(() -> "No logs found.");
                appendToLogBuffer("No logs found.");
                Utils.showToastLong("No logs found!");
            }
        } catch (Exception e) {
            Logger.printDebug(() -> "Failed to export logs: " + e.getMessage());
            appendToLogBuffer("Failed to export logs: " + e.getMessage());
            Utils.showToastLong("Failed to export logs: " + e.getMessage());
        }
    }

    /**
     * Injection point.
     */
    public static boolean isBooleanFeatureFlagEnabled(boolean value, Long flag) {
        if (LOG_FEATURE_FLAGS && value) {
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
