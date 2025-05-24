package app.revanced.extension.shared.settings.preference;

import static app.revanced.extension.shared.StringRef.str;

import java.util.Deque;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.atomic.AtomicInteger;

import app.revanced.extension.shared.Logger;
import app.revanced.extension.shared.Utils;
import app.revanced.extension.shared.settings.BaseSettings;

/**
 * Manages a buffer for storing debug logs from Logger.
 * Thread-safe and limits buffer to approximately {@link #BUFFER_MAX_BYTES} to avoid TransactionTooLargeException.
 */
public final class LogBufferManager {
    /** Maximum byte size of all buffer entries. Must be less than Android's 1 MB Binder transaction limit. */
    private static final int BUFFER_MAX_BYTES = 900_000;
    /** Limit number of log lines. */
    private static final int BUFFER_MAX_SIZE = 10_000;

    private static final Deque<String> logBuffer = new ConcurrentLinkedDeque<>();
    private static final AtomicInteger logBufferByteSize = new AtomicInteger();

    /**
     * Appends a log message to the internal buffer if debugging is enabled.
     * The buffer is limited to approximately {@link #BUFFER_MAX_BYTES} or {@link #BUFFER_MAX_SIZE}
     * to prevent excessive memory usage. This method is thread-safe.
     *
     * @param message The log message to append. Ignored if null or empty.
     */
    public static void appendToLogBuffer(String message) {
        if (!BaseSettings.DEBUG.get()) return;

        logBuffer.addLast(message);
        int newSize = logBufferByteSize.addAndGet(message.length());

        while (newSize > BUFFER_MAX_BYTES || logBuffer.size() > BUFFER_MAX_SIZE) {
            String removed = logBuffer.pollFirst();
            if (removed == null) {
                // Thread race of two different calls to this method,
                // and the other thread won.
                return;
            }

            newSize = logBufferByteSize.addAndGet(-removed.length());
        }
    }

    /**
     * Exports all logs from the internal buffer to the clipboard and optionally clears the buffer.
     * Displays a toast with the result. This method is thread-safe.
     */
    public static void exportToClipboard() {
        if (!BaseSettings.DEBUG.get()) {
            Utils.showToastShort(str("revanced_debug_logs_disabled"));
            return;
        }

        if (logBuffer.isEmpty()) {
            Utils.showToastShort(str("revanced_debug_logs_none_found"));
            clearLogBufferData(); // Clear toast log entry that was created.
            return;
        }

        try {
            // Most (but not all) Android 13+ devices always show a "copied to clipboard" toast
            // and there is no way to programmatically detect if a toast will show or not.
            // Show a toast even if using Android 13+, but show ReVanced toast first (before copying to clipboard).
            Utils.showToastShort(str("revanced_debug_logs_copied_to_clipboard"));

            Utils.setClipboard(String.join("\n", logBuffer));
        } catch (Exception ex) {
            // Handle security exception if clipboard access is denied.
            String errorMessage = String.format(str("revanced_debug_logs_failed_to_export"), ex.getMessage());
            Utils.showToastLong(errorMessage);
            Logger.printDebug(() -> errorMessage, ex);
        }
    }

    private static void clearLogBufferData() {
        // Cannot simply clear the log buffer because there is no
        // write lock for both the deque and the atomic int.
        // Instead pop off log entries and decrement their size one by one.
        while (!logBuffer.isEmpty()) {
            String removed = logBuffer.pollFirst();
            if (removed != null) {
                logBufferByteSize.addAndGet(-removed.length());
            }
        }
    }

    /**
     * Clears the internal log buffer and displays a toast with the result.
     * This method is thread-safe.
     */
    public static void clearLogBuffer() {
        if (!BaseSettings.DEBUG.get()) {
            Utils.showToastShort(str("revanced_debug_logs_disabled"));
            return;
        }

        try {
            // Show toast before clearing, otherwise toast log will still remain.
            Utils.showToastShort(str("revanced_debug_logs_clear_toast"));
            clearLogBufferData();
        } catch (Exception ex) {
            Logger.printException(() -> "clearLogBuffer failure", ex);
        }
    }
}
