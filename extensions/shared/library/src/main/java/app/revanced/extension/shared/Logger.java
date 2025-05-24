package app.revanced.extension.shared;

import static app.revanced.extension.shared.settings.BaseSettings.DEBUG;
import static app.revanced.extension.shared.settings.BaseSettings.DEBUG_STACKTRACE;
import static app.revanced.extension.shared.settings.BaseSettings.DEBUG_TOAST_ON_ERROR;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.io.PrintWriter;
import java.io.StringWriter;

import app.revanced.extension.shared.settings.BaseSettings;
import app.revanced.extension.shared.settings.preference.LogBufferManager;

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

    private enum LogLevel {
        DEBUG,
        INFO,
        ERROR
    }

    private static final String REVANCED_LOG_PREFIX = "revanced: ";

    /**
     * Internal method to handle logging to Android Log and LogBufferManager.
     * Appends the log message, stack trace (if enabled), and exception (if present) to logBuffer
     * with class name but without 'revanced:' prefix.
     *
     * @param logLevel The log level.
     * @param message  Log message object.
     * @param ex       Optional exception.
     */
    private static void logInternal(LogLevel logLevel, LogMessage message, @Nullable Throwable ex) {
        String messageString = message.buildMessageString();
        String className = message.findOuterClassSimpleName();
        String logTag = REVANCED_LOG_PREFIX + className;
        String classNameMessage = className + ": " + messageString;

        String logText = classNameMessage;

        if (DEBUG_STACKTRACE.get()) {
            var sw = new StringWriter();
            new Throwable().printStackTrace(new PrintWriter(sw));
            String stackTrace = sw.toString();
            // Trim off first line of "java.lang.Throwable"
            logText += stackTrace.substring(stackTrace.indexOf('\n'));
        }

        LogBufferManager.appendToLogBuffer(logText);

        // Append exception message to logBuffer if present.
        if (ex != null && ex.getMessage() != null) {
            LogBufferManager.appendToLogBuffer(className + ": Exception: " + ex.getMessage());
        }

        switch (logLevel) {
            case DEBUG:
                if (ex == null) Log.d(logTag, logText);
                else Log.d(logTag, logText, ex);
                break;
            case INFO:
                if (ex == null) Log.i(logTag, logText);
                else Log.i(logTag, logText, ex);
                break;
            case ERROR:
                if (ex == null) Log.e(logTag, logText);
                else Log.e(logTag, logText, ex);

                if (DEBUG_TOAST_ON_ERROR.get()) {
                    Utils.showToastLong(classNameMessage);
                }
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
            logInternal(LogLevel.DEBUG, message, ex);
        }
    }

    /**
     * Logs information messages using the outer class name of the code calling this method.
     * Appends the log message and exception (if present) to logBuffer.
     */
    public static void printInfo(LogMessage message) {
        printInfo(message, null);
    }

    /**
     * Logs information messages using the outer class name of the code calling this method.
     * Appends the log message and exception (if present) to logBuffer.
     */
    public static void printInfo(LogMessage message, @Nullable Exception ex) {
        logInternal(LogLevel.INFO, message, ex);
    }

    /**
     * Logs exceptions under the outer class name of the code calling this method.
     * Appends the log message, exception (if present), and toast message (if enabled) to logBuffer.
     */
    public static void printException(LogMessage message) {
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
    public static void printException(LogMessage message, @Nullable Throwable ex) {
        logInternal(LogLevel.ERROR, message, ex);
    }

    /**
     * Logging to use if {@link BaseSettings#DEBUG} or {@link Utils#getContext()} may not be initialized.
     * Appends the log message to logBuffer.
     * Normally this method should not be used.
     */
    public static void initializationInfo(Class<?> callingClass, String message) {
        String className = callingClass.getSimpleName();
        String logTag = REVANCED_LOG_PREFIX + className;

        // Append log message with class name to logBuffer.
        LogBufferManager.appendToLogBuffer(className + ": " + message);
        Log.i(logTag, message);
    }

    /**
     * Logging to use if {@link BaseSettings#DEBUG} or {@link Utils#getContext()} may not be initialized.
     * Appends the log message and exception (if present) to logBuffer.
     * Normally this method should not be used.
     */
    public static void initializationException(Class<?> callingClass, String message,
                                               @Nullable Exception ex) {
        String className = callingClass.getSimpleName();
        String logTag = REVANCED_LOG_PREFIX + className;
        // Append log message with class name to logBuffer.
        LogBufferManager.appendToLogBuffer(className + ": " + message);

        // Append exception message to logBuffer if present
        if (ex != null && ex.getMessage() != null) {
            LogBufferManager.appendToLogBuffer(className + ": Exception: " + ex.getMessage());
        }

        Log.e(logTag, message, ex);
    }
}
