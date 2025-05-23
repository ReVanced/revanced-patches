package app.revanced.extension.shared.settings.preference;

import static app.revanced.extension.shared.StringRef.str;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import app.revanced.extension.shared.Logger;
import app.revanced.extension.shared.Utils;
import app.revanced.extension.shared.settings.BaseSettings;

/**
 * Manages a buffer for storing debug logs from Logger.printDebug.
 */
@SuppressWarnings("unused")
public final class LogBufferManager {

    /**
     * Only log if debugging is enabled on startup.
     * This prevents enabling debugging while the app is running then failing to restart
     * resulting in an incomplete log.
     */
    private static final boolean isDebugEnabled = BaseSettings.DEBUG.get();

    private static final int MAX_LOG_BYTES = 1_048_576; // 1 MB
    private static final StringBuilder logBuffer = isDebugEnabled ? new StringBuilder() : null;

    /**
     * Appends a log message to the internal buffer.
     * Limits the buffer to 1 MB to prevent excessive memory usage.
     * Called by Logger.printDebug and other components.
     *
     * @param message The log message to append.
     */
    public static void appendToLogBuffer(String message) {
        if (isDebugEnabled && logBuffer != null) {
            synchronized (logBuffer) {
                String logEntry = message + "\n";
                logBuffer.append(logEntry);
                // Limit buffer to MAX_LOG_BYTES
                try {
                    while (logBuffer.toString().getBytes("UTF-8").length > MAX_LOG_BYTES) {
                        // Find the first newline to remove the oldest log entry
                        int firstNewline = logBuffer.indexOf("\n");
                        if (firstNewline >= 0) {
                            logBuffer.delete(0, firstNewline + 1);
                        } else {
                            // No newlines left, clear the entire buffer
                            logBuffer.setLength(0);
                        }
                    }
                } catch (Exception e) {
                    Logger.printDebug(() -> "Failed to trim log buffer: " + e.getMessage());
                    logBuffer.setLength(0); // Clear buffer on error to prevent issues
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
    public static void exportToClipboard(Context context) {
        if (!isDebugEnabled || logBuffer == null) {
            Utils.showToastLong(str("revanced_debug_logging_disabled"));
            return;
        }
        try {
            StringBuilder exportedLogs = new StringBuilder();
            synchronized (logBuffer) {
                exportedLogs.append(logBuffer.toString());
                logBuffer.setLength(0); // Clear buffer after export
            }
            String errorMessage;
            if (exportedLogs.length() > 0) {
                ClipboardManager clipboard = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText("ReVanced Logs", exportedLogs.toString());
                clipboard.setPrimaryClip(clip);
                errorMessage = str("revanced_debug_logs_copied_to_clipboard");
            } else {
                errorMessage = str("revanced_debug_no_logs_found");
            }
            Logger.printDebug(() -> errorMessage);
            appendToLogBuffer(errorMessage);
            Utils.showToastLong(errorMessage);
        } catch (Exception e) {
            String errorMessage = String.format(str("revanced_debug_failed_to_export_logs"), e.getMessage());
            Logger.printDebug(() -> errorMessage);
            appendToLogBuffer(errorMessage);
            Utils.showToastLong(errorMessage);
        }
    }
}
