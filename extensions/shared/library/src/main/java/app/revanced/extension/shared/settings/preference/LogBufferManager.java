package app.revanced.extension.shared.settings.preference;

import static app.revanced.extension.shared.StringRef.str;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import app.revanced.extension.shared.Logger;
import app.revanced.extension.shared.Utils;
import app.revanced.extension.shared.settings.BaseSettings;

import java.util.ArrayDeque;

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
    private static final int MAX_LOG_BYTES = 1_000_000; // ~1 MB
    private static final ArrayDeque<String> logBuffer = isDebugEnabled ? new ArrayDeque<>() : null;
    private static int estimatedByteSize = 0;

    /**
     * Appends a log message to the internal buffer.
     * Limits the buffer to 1 MB to prevent excessive memory usage.
     * Called by Logger.printDebug and other components.
     *
     * @param message The log message to append.
     */
    public static void appendToLogBuffer(String message) {
        if (!isDebugEnabled || logBuffer == null || message == null || message.isEmpty()) return;

        synchronized (logBuffer) {
            String logEntry = message + "\n";
            logBuffer.addLast(logEntry);
            estimatedByteSize += logEntry.length();

            while (estimatedByteSize > MAX_LOG_BYTES) {
                String removed = logBuffer.pollFirst();
                if (removed != null) {
                    estimatedByteSize -= removed.length();
                } else {
                    estimatedByteSize = 0;
                    break;
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
            String logs;
            synchronized (logBuffer) {
                logs = String.join("", logBuffer);
                logBuffer.clear();
                estimatedByteSize = 0;
            }
            String message;
            if (!logs.isEmpty()) {
                ClipboardManager clipboard = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText("ReVanced Logs", logs);
                clipboard.setPrimaryClip(clip);
                message = str("revanced_debug_logs_copied_to_clipboard");
            } else {
                message = str("revanced_debug_no_logs_found");
            }
            Logger.printDebug(() -> message);
            Utils.showToastLong(message);
        } catch (Exception e) {
            String errorMessage = String.format(str("revanced_debug_failed_to_export_logs"), e.getMessage());
            Logger.printDebug(() -> errorMessage);
            Utils.showToastLong(errorMessage);
        }
    }
}
