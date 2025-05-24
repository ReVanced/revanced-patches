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
    }

    private enum LogLevel {
        DEBUG,
        INFO,
        ERROR
    }

    private static final String REVANCED_LOG_PREFIX = "revanced: ";

    /**
     * @return For outer classes, this returns {@link Class#getSimpleName()}.
     * For static, inner, or anonymous classes, this returns the simple name of the enclosing class.
     * <br>
     * For example, each of these classes returns 'SomethingView':
     * <code>
     * com.company.SomethingView
     * com.company.SomethingView$StaticClass
     * com.company.SomethingView$1
     * </code>
     */
    private static String findOuterClassSimpleName(Object obj) {
        Class<?> logClass = obj.getClass();
        String fullClassName = logClass.getName();
        final int dollarSignIndex = fullClassName.indexOf('$');
        if (dollarSignIndex < 0) {
            return logClass.getSimpleName(); // Already an outer class.
        }

        // Class is inner, static, or anonymous.
        // Parse the simple name full name.
        // A class with no package returns index of -1, but incrementing gives index zero which is correct.
        final int simpleClassNameStartIndex = fullClassName.lastIndexOf('.') + 1;
        return fullClassName.substring(simpleClassNameStartIndex, dollarSignIndex);
    }

    /**
     * Internal method to handle logging to Android Log and {@link LogBufferManager}.
     * Appends the log message, stack trace (if enabled), and exception (if present) to logBuffer
     * with class name but without 'revanced:' prefix.
     *
     * @param logLevel          The log level.
     * @param message           Log message object.
     * @param ex                Optional exception.
     * @param includeStackTrace If the current stack should be included.
     * @param showToast         If a toast is to be shown.
     */
    private static void logInternal(LogLevel logLevel, LogMessage message, @Nullable Throwable ex,
                                    boolean includeStackTrace, boolean showToast) {
        // It's very important that no Settings are used in this method,
        // as this code is used when a context is not set and thus referencing
        // a setting will crash the app.
        String messageString = message.buildMessageString();
        String className = findOuterClassSimpleName(message);
        String logTag = REVANCED_LOG_PREFIX + className;
        String classNameMessage = className + ": " + messageString;

        String logText = classNameMessage;

        // Append exception message to logBuffer if present.
        if (ex != null && ex.getMessage() != null) {
            logText += "\nException: " + ex.getMessage();
        }

        if (includeStackTrace) {
            var sw = new StringWriter();
            new Throwable().printStackTrace(new PrintWriter(sw));
            String stackTrace = sw.toString();
            // Trim off first line of "java.lang.Throwable"
            logText += stackTrace.substring(stackTrace.indexOf('\n'));
        }

        LogBufferManager.appendToLogBuffer(logText);

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
                break;
        }

        if (showToast) {
            Utils.showToastLong(classNameMessage);
        }
    }

    /**
     * Logs debug messages under the outer class name of the code calling this method.
     * <p>
     * Whenever possible, the log string should be constructed entirely inside
     * {@link LogMessage#buildMessageString()} so the performance cost of
     * building strings is paid only if {@link BaseSettings#DEBUG} is enabled.
     */
    public static void printDebug(@NonNull LogMessage message) {
        printDebug(message, null);
    }

    /**
     * Logs debug messages under the outer class name of the code calling this method.
     * <p>
     * Whenever possible, the log string should be constructed entirely inside
     * {@link LogMessage#buildMessageString()} so the performance cost of
     * building strings is paid only if {@link BaseSettings#DEBUG} is enabled.
     */
    public static void printDebug(@NonNull LogMessage message, @Nullable Exception ex) {
        if (DEBUG.get()) {
            logInternal(LogLevel.DEBUG, message, ex, DEBUG_STACKTRACE.get(), false);
        }
    }

    /**
     * Logs information messages using the outer class name of the code calling this method.
     */
    public static void printInfo(LogMessage message) {
        printInfo(message, null);
    }

    /**
     * Logs information messages using the outer class name of the code calling this method.
     */
    public static void printInfo(LogMessage message, @Nullable Exception ex) {
        logInternal(LogLevel.INFO, message, ex, DEBUG_STACKTRACE.get(), false);
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
     * <p>
     * If the calling code is showing it's own error toast,
     * instead use {@link #printInfo(LogMessage, Exception)}
     *
     * @param message          log message
     * @param ex               exception (optional)
     */
    public static void printException(LogMessage message, @Nullable Throwable ex) {
        logInternal(LogLevel.ERROR, message, ex, DEBUG_STACKTRACE.get(), DEBUG_TOAST_ON_ERROR.get());
    }

    /**
     * Logging to use if {@link BaseSettings#DEBUG} or {@link Utils#getContext()} may not be initialized.
     * Normally this method should not be used.
     */
    public static void initializationInfo(LogMessage message) {
        logInternal(LogLevel.INFO, message, null, false, false);
    }

    /**
     * Logging to use if {@link BaseSettings#DEBUG} or {@link Utils#getContext()} may not be initialized.
     * Normally this method should not be used.
     */
    public static void initializationException(LogMessage message, @Nullable Exception ex) {
        logInternal(LogLevel.ERROR, message, ex, false, false);
    }
}
