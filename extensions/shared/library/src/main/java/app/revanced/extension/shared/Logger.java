package app.revanced.extension.shared;

import android.util.Log;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import app.revanced.extension.shared.settings.BaseSettings;
import app.revanced.extension.shared.settings.preference.LogBufferManager;

import java.io.PrintWriter;
import java.io.StringWriter;

import static app.revanced.extension.shared.settings.BaseSettings.*;

/**
 * Logger class for ReVanced, handling debug, info, exception and initialization logs.
 * All log messages are appended to logBuffer for export.
 */
public class Logger {

    /**
     * Log messages using lambdas.
     */
    @FunctionalInterface
    public interface LogMessage {
        @NonNull
        String buildMessageString();

        /**
         * @return For outer classes, this returns {@link Class#getSimpleName()}.
         * For static, inner, or anonymous classes, this returns the simple name of the enclosing class.
         * <br>
         * For example, each of these classes return 'SomethingView':
         * <code>
         * com.company.SomethingView
         * com.company.SomethingView$StaticClass
         * com.company.SomethingView$1
         * </code>
         */
        private String findOuterClassSimpleName() {
            var selfClass = this.getClass();

            String fullClassName = selfClass.getName();
            final int dollarSignIndex = fullClassName.indexOf('$');
            if (dollarSignIndex < 0) {
                return selfClass.getSimpleName(); // Already an outer class.
            }

            // Class is inner, static, or anonymous.
            // Parse the simple name full name.
            // A class with no package returns index of -1, but incrementing gives index zero which is correct.
            final int simpleClassNameStartIndex = fullClassName.lastIndexOf('.') + 1;
            return fullClassName.substring(simpleClassNameStartIndex, dollarSignIndex);
        }
    }

    private static final String REVANCED_LOG_PREFIX = "revanced: ";

    /**
     * Internal method to handle logging to Android Log and LogBufferManager.
     * Appends the log message, stack trace (if enabled), and exception (if present) to logBuffer
     * with class name but without 'revanced:' prefix.
     *
     * @param logTag           The log tag including 'revanced:' prefix for Android Log.
     * @param className        The class name for logBuffer (without prefix).
     * @param message          The log message.
     * @param ex               Optional exception.
     * @param logLevel         The log level ("DEBUG", "INFO", "ERROR").
     * @param includeStackTrace Whether to include stack trace (for debug logs).
     */
    private static void logInternal(@NonNull String logTag, @NonNull String className, @NonNull String message,
                                    @Nullable Throwable ex, @NonNull String logLevel, boolean includeStackTrace) {
        // Append log message with class name to logBuffer
        LogBufferManager.appendToLogBuffer(className + ": " + message);

        if (includeStackTrace && DEBUG_STACKTRACE.get()) {
            var sw = new StringWriter();
            new Throwable().printStackTrace(new PrintWriter(sw));
            LogBufferManager.appendToLogBuffer(className + ": StackTrace: " + sw.toString());
        }

        // Append exception message to logBuffer if present
        if (ex != null && ex.getMessage() != null) {
            LogBufferManager.appendToLogBuffer(className + ": Exception: " + ex.getMessage());
        }

        switch (logLevel) {
            case "DEBUG":
                if (ex == null) Log.d(logTag, message);
                else Log.d(logTag, message, ex);
                break;
            case "INFO":
                if (ex == null) Log.i(logTag, message);
                else Log.i(logTag, message, ex);
                break;
            case "ERROR":
                if (ex == null) Log.e(logTag, message);
                else Log.e(logTag, message, ex);
                break;
        }
    }

    /**
     * Logs debug messages under the outer class name of the code calling this method.
     * Appends the log message, stack trace (if enabled), and exception (if present) to logBuffer
     * if debugging is enabled.
     * Whenever possible, the log string should be constructed entirely inside {@link LogMessage#buildMessageString()}
     * so the performance cost of building strings is paid only if {@link BaseSettings#DEBUG} is enabled.
     */
    public static void printDebug(@NonNull LogMessage message) {
        printDebug(message, null);
    }

    /**
     * Logs debug messages under the outer class name of the code calling this method.
     * Appends the log message, stack trace (if enabled), and exception (if present) to logBuffer
     * if debugging is enabled.
     * Whenever possible, the log string should be constructed entirely inside {@link LogMessage#buildMessageString()}
     * so the performance cost of building strings is paid only if {@link BaseSettings#DEBUG} is enabled.
     */
    public static void printDebug(@NonNull LogMessage message, @Nullable Exception ex) {
        if (DEBUG.get()) {
            String className = message.findOuterClassSimpleName();
            String logTag = REVANCED_LOG_PREFIX + className;
            logInternal(logTag, className, message.buildMessageString(), ex, "DEBUG", true);
        }
    }

    /**
     * Logs information messages using the outer class name of the code calling this method.
     * Appends the log message and exception (if present) to logBuffer.
     */
    public static void printInfo(@NonNull LogMessage message) {
        printInfo(message, null);
    }

    /**
     * Logs information messages using the outer class name of the code calling this method.
     * Appends the log message and exception (if present) to logBuffer.
     */
    public static void printInfo(@NonNull LogMessage message, @Nullable Exception ex) {
        String className = message.findOuterClassSimpleName();
        String logTag = REVANCED_LOG_PREFIX + className;
        logInternal(logTag, className, message.buildMessageString(), ex, "INFO", false);
    }

    /**
     * Logs exceptions under the outer class name of the code calling this method.
     * Appends the log message, exception (if present), and toast message (if enabled) to logBuffer.
     */
    public static void printException(@NonNull LogMessage message) {
        printException(message, null);
    }

    /**
     * Logs exceptions under the outer class name of the code calling this method.
     * Appends the log message, exception (if present), and toast message (if enabled) to logBuffer.
     * <p>
     * If the calling code is showing it's own error toast,
     * instead use {@link #printInfo(LogMessage, Exception)}
     *
     * @param message          log message
     * @param ex               exception (optional)
     */
    public static void printException(@NonNull LogMessage message, @Nullable Throwable ex) {
        String messageString = message.buildMessageString();
        String className = message.findOuterClassSimpleName();
        String logTag = REVANCED_LOG_PREFIX + className;
        logInternal(logTag, className, messageString, ex, "ERROR", false);

        if (DEBUG_TOAST_ON_ERROR.get()) {
            Utils.showToastLong(className + ": " + messageString);
        }
    }

    /**
     * Logging to use if {@link BaseSettings#DEBUG} or {@link Utils#getContext()} may not be initialized.
     * Appends the log message to logBuffer.
     * Normally this method should not be used.
     */
    public static void initializationInfo(@NonNull Class<?> callingClass, @NonNull String message) {
        String className = callingClass.getSimpleName();
        String logTag = REVANCED_LOG_PREFIX + className;
        // Append log message with class name to logBuffer
        LogBufferManager.appendToLogBuffer(className + ": " + message);
        Log.i(logTag, message);
    }

    /**
     * Logging to use if {@link BaseSettings#DEBUG} or {@link Utils#getContext()} may not be initialized.
     * Appends the log message and exception (if present) to logBuffer.
     * Normally this method should not be used.
     */
    public static void initializationException(@NonNull Class<?> callingClass, @NonNull String message,
                                               @Nullable Exception ex) {
        String className = callingClass.getSimpleName();
        String logTag = REVANCED_LOG_PREFIX + className;
        // Append log message with class name to logBuffer
        LogBufferManager.appendToLogBuffer(className + ": " + message);

        // Append exception message to logBuffer if present
        if (ex != null && ex.getMessage() != null) {
            LogBufferManager.appendToLogBuffer(className + ": Exception: " + ex.getMessage());
        }

        Log.e(logTag, message, ex);
    }
}
