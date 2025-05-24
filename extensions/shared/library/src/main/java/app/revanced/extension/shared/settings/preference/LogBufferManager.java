package app.revanced.extension.shared.settings.preference;

import static app.revanced.extension.shared.StringRef.str;

import android.os.Build;

import java.util.Deque;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.atomic.AtomicInteger;

import app.revanced.extension.shared.Logger;
import app.revanced.extension.shared.Utils;
import app.revanced.extension.shared.settings.BaseSettings;

/**
 * Manages a buffer for storing debug logs from Logger.printDebug.
 * Thread-safe and limits buffer to approximately @MAX_LOG_BYTES to avoid TransactionTooLargeException.
 */
public final class LogBufferManager {
    private static final int MAX_LOG_BYTES = 900_000; // Approximately 900 KB, below Android's 1 MB Binder transaction limit.
    private static final int MAX_LOG_LINES = 10_000; // Limit number of log lines.
    private static final Deque<String> logBuffer = new ConcurrentLinkedDeque<>();
    private static final AtomicInteger estimatedByteSize = new AtomicInteger();

    /**
     * Appends a log message to the internal buffer if debugging is enabled.
     * The buffer is limited to approximately @MAX_LOG_BYTES or @MAX_LOG_LINES to prevent excessive memory usage.
     * This method is thread-safe.
     *
     * @param message The log message to append. Ignored if null or empty.
     */
    public static void appendToLogBuffer(String message) {
        if (!BaseSettings.DEBUG.get()) return;

        logBuffer.addLast(message);
        int newSize = estimatedByteSize.addAndGet(message.length());

        while (newSize > MAX_LOG_BYTES || logBuffer.size() > MAX_LOG_LINES) {
            String removed = logBuffer.pollFirst();
            if (removed == null) {
                // Should never be reached.
                estimatedByteSize.set(0);
                break;
            } else {
                newSize = estimatedByteSize.addAndGet(-removed.length());
            }
        }
    }

    /**
     * Exports all logs from the internal buffer to the clipboard and optionally clears the buffer.
     * Displays a toast with the result. This method is thread-safe.
     */
    public static void exportToClipboard() {
        if (!BaseSettings.DEBUG.get()) {
            Utils.showToastLong(str("revanced_debug_logs_disabled"));
            return;
        }

        try {
            Utils.setClipboard(String.join("\n", logBuffer));

            // Only show a toast if using Android 12 or earlier since Android 13+ already shows a toast.
            if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.S_V2) {
                Utils.showToastLong(str("revanced_debug_logs_copied_to_clipboard"));
            }
        } catch (Exception e) {
            // Handle security exception if clipboard access is denied.
            String errorMessage = String.format(str("revanced_debug_logs_failed_to_export"), e.getMessage());
            Utils.showToastLong(errorMessage);
            Logger.printDebug(() -> errorMessage, e);
        }
    }

    private static void clearLogBufferData() {
        logBuffer.clear();

        // Do not reset to zero, otherwise theoretically another thread
        // could set a value between this call and the clear() above.
        estimatedByteSize.addAndGet(-estimatedByteSize.get());
    }

    /**
     * Clears the internal log buffer and displays a toast with the result.
     * This method is thread-safe.
     */
    public static void clearLogBuffer() {
        if (!BaseSettings.DEBUG.get()) {
            Utils.showToastLong(str("revanced_debug_logs_disabled"));
            return;
        }

        try {
            clearLogBufferData();
            Utils.showToastLong(str("revanced_debug_logs_cleared"));
        } catch (Exception ex) {
            Logger.printException(() -> "clearLogBuffer failure", ex);
        }
    }
}
