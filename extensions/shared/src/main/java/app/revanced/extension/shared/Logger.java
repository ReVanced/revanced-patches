package app.revanced.extension.shared;

import android.util.Log;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import app.revanced.extension.shared.settings.BaseSettings;

import java.io.PrintWriter;
import java.io.StringWriter;

import static app.revanced.extension.shared.settings.BaseSettings.*;

public class Logger {

    /**
     * Log messages using lambdas.
     */
    @FunctionalInterface
    public interface LogMessage {
        @NonNull
        String buildMessageString();
    }

    private static final String REVANCED_LOG_PREFIX = "revanced: ";

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
    private static String findOuterClassSimpleName(Object obj) {
        var selfClass = obj.getClass();
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


    /**
     * Logs debug messages under the outer class name of the code calling this method.
     * Whenever possible, the log string should be constructed entirely inside {@link LogMessage#buildMessageString()}
     * so the performance cost of building strings is paid only if {@link BaseSettings#DEBUG} is enabled.
     */
    public static void printDebug(@NonNull LogMessage message) {
        printDebug(message, null);
    }

    /**
     * Logs debug messages under the outer class name of the code calling this method.
     * Whenever possible, the log string should be constructed entirely inside {@link LogMessage#buildMessageString()}
     * so the performance cost of building strings is paid only if {@link BaseSettings#DEBUG} is enabled.
     */
    public static void printDebug(@NonNull LogMessage message, @Nullable Exception ex) {
        if (DEBUG.get()) {
            String logMessage = message.buildMessageString();
            String logTag = REVANCED_LOG_PREFIX + findOuterClassSimpleName(message);

            if (DEBUG_STACKTRACE.get()) {
                var builder = new StringBuilder(logMessage);
                var sw = new StringWriter();
                new Throwable().printStackTrace(new PrintWriter(sw));

                builder.append('\n').append(sw);
                logMessage = builder.toString();
            }

            if (ex == null) {
                Log.d(logTag, logMessage);
            } else {
                Log.d(logTag, logMessage, ex);
            }
        }
    }

    /**
     * Logs information messages using the outer class name of the code calling this method.
     */
    public static void printInfo(@NonNull LogMessage message) {
        printInfo(message, null);
    }

    /**
     * Logs information messages using the outer class name of the code calling this method.
     */
    public static void printInfo(@NonNull LogMessage message, @Nullable Exception ex) {
        String logTag = REVANCED_LOG_PREFIX + findOuterClassSimpleName(message);
        String logMessage = message.buildMessageString();
        if (ex == null) {
            Log.i(logTag, logMessage);
        } else {
            Log.i(logTag, logMessage, ex);
        }
    }

    /**
     * Logs exceptions under the outer class name of the code calling this method.
     */
    public static void printException(@NonNull LogMessage message) {
        printException(message, null, null);
    }

    /**
     * Logs exceptions under the outer class name of the code calling this method.
     */
    public static void printException(@NonNull LogMessage message, @Nullable Throwable ex) {
        printException(message, ex, null);
    }

    /**
     * Logs exceptions under the outer class name of the code calling this method.
     * <p>
     * If the calling code is showing it's own error toast,
     * instead use {@link #printInfo(LogMessage, Exception)}
     *
     * @param message          log message
     * @param ex               exception (optional)
     * @param userToastMessage user specific toast message to show instead of the log message (optional)
     */
    public static void printException(@NonNull LogMessage message, @Nullable Throwable ex,
                                      @Nullable String userToastMessage) {
        String messageString = message.buildMessageString();
        String outerClassSimpleName = findOuterClassSimpleName(message);
        String logMessage = REVANCED_LOG_PREFIX + outerClassSimpleName;
        if (ex == null) {
            Log.e(logMessage, messageString);
        } else {
            Log.e(logMessage, messageString, ex);
        }
        if (DEBUG_TOAST_ON_ERROR.get()) {
            String toastMessageToDisplay = (userToastMessage != null)
                    ? userToastMessage
                    : outerClassSimpleName + ": " + messageString;
            Utils.showToastLong(toastMessageToDisplay);
        }
    }

    /**
     * Logging to use if {@link BaseSettings#DEBUG} or {@link Utils#getContext()} may not be initialized.
     * Normally this method should not be used.
     */
    public static void initializationInfo(@NonNull Class<?> callingClass, @NonNull String message) {
        Log.i(REVANCED_LOG_PREFIX + callingClass.getSimpleName(), message);
    }

    /**
     * Logging to use if {@link BaseSettings#DEBUG} or {@link Utils#getContext()} may not be initialized.
     * Normally this method should not be used.
     */
    public static void initializationException(@NonNull Class<?> callingClass, @NonNull String message,
                                               @Nullable Exception ex) {
        Log.e(REVANCED_LOG_PREFIX + callingClass.getSimpleName(), message, ex);
    }

}