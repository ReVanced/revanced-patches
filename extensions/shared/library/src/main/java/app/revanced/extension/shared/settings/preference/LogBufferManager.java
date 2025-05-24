package app.revanced.extension.shared.settings.preference;

import static app.revanced.extension.shared.StringRef.str;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import app.revanced.extension.shared.Logger;
import app.revanced.extension.shared.Utils;
import app.revanced.extension.shared.settings.BaseSettings;

import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Manages a buffer for storing debug logs from Logger.printDebug.
 * Thread-safe and limits buffer to approximately @MAX_LOG_BYTES to avoid TransactionTooLargeException.
 */
public final class LogBufferManager {
    private static final boolean IS_DEBUG_ENABLED = BaseSettings.DEBUG.get();
    private static final int MAX_LOG_BYTES = 900_000; // Approximately 900 KB, below Android's 1 MB Binder transaction limit.
    private static final int MAX_LOG_LINES = 10_000; // Limit number of log lines.
    private static final ConcurrentLinkedDeque<String> logBuffer = IS_DEBUG_ENABLED ? new ConcurrentLinkedDeque<>() : null;
    private static final AtomicInteger estimatedByteSize = IS_DEBUG_ENABLED ? new AtomicInteger(0) : null;

    /**
     * Appends a log message to the internal buffer if debugging is enabled.
     * The buffer is limited to approximately @MAX_LOG_BYTES or @MAX_LOG_LINES to prevent excessive memory usage.
     * This method is thread-safe.
     *
     * @param message The log message to append. Ignored if null or empty.
     */
    public static void appendToLogBuffer(String message) {
        if (!IS_DEBUG_ENABLED || logBuffer == null || message == null || message.isEmpty()) return;

        String logEntry = message + "\n";
        logBuffer.addLast(logEntry);
        int newSize = estimatedByteSize.addAndGet(logEntry.length());

        while (newSize > MAX_LOG_BYTES || logBuffer.size() > MAX_LOG_LINES) {
            String removed = logBuffer.pollFirst();
            if (removed != null) {
                newSize = estimatedByteSize.addAndGet(-removed.length());
            } else {
                estimatedByteSize.set(0);
                break;
            }
        }
    }

    /**
     * Exports all logs from the internal buffer to the clipboard and optionally clears the buffer.
     * Displays a toast with the result. This method is thread-safe.
     *
     * @param context     The Android context for accessing the clipboard and showing toasts.
     * @param clearBuffer Whether to clear the buffer after exporting.
     * @throws SecurityException If clipboard access is denied.
     */
    public static void exportToClipboard(Context context, boolean clearBuffer) {
        if (!IS_DEBUG_ENABLED || logBuffer == null) {
            Utils.showToastLong(str("revanced_debug_logging_disabled"));
            return;
        }

        try {
            String logs = String.join("", logBuffer);
            if (clearBuffer) {
                logBuffer.clear();
                estimatedByteSize.set(0);
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
            Logger.printException(() -> errorMessage, e);
            Utils.showToastLong(errorMessage);
        }
    }
}
