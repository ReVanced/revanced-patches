package app.revanced.integrations.utils;

import android.util.Log;

import java.io.PrintWriter;
import java.io.StringWriter;

import app.revanced.integrations.settings.SettingsEnum;

public class LogHelper {

    /**
     * Log messages using lambdas.
     */
    public interface LogMessage {
        String buildMessageString();

        /**
         * @return For non inner classes, this returns {@link Class#getSimpleName()}.
         * For inner classes (static and anonymous), this returns the enclosing class simple name.<br>
         * <br>
         * For example, each of these classes return 'SomethingView':<br>
         * com.company.SomethingView<br>
         * com.company.SomethingView$StaticClass<br>
         * com.company.SomethingView$1<br>
         */
        private String findOuterClassSimpleName() {
            var selfClass = this.getClass();

            String fullClassName = selfClass.getName();
            final int dollarSignIndex = fullClassName.indexOf('$');
            if (dollarSignIndex == -1) {
                return selfClass.getSimpleName(); // already an outer class
            }
            // else, class is inner class (static or anonymous)

            // parse the simple name full name
            // a class with no package returns index of -1, but incrementing gives index zero which is correct
            final int simpleClassNameStartIndex = fullClassName.lastIndexOf('.') + 1;
            return fullClassName.substring(simpleClassNameStartIndex, dollarSignIndex);
        }
    }

    /**
     * Logs information messages with the most outer class name of the code that is calling this method.
     */
    public static void printInfo(LogMessage message) {
        Log.i("revanced: " + message.findOuterClassSimpleName(), message.buildMessageString());
    }

    /**
     * Logs debug messages with the most outer class name of the code that is calling this method.
     */
    public static void printDebug(LogMessage message) {
        if (SettingsEnum.DEBUG.getBoolean()) {
            var messageString = message.buildMessageString();

            if (SettingsEnum.DEBUG_STACKTRACE.getBoolean()) {
                var builder = new StringBuilder(messageString);
                var sw = new StringWriter();
                new Throwable().printStackTrace(new PrintWriter(sw));

                builder.append(String.format("\n%s", sw));
                messageString = builder.toString();
            }

            Log.d("revanced: " + message.findOuterClassSimpleName(), messageString);
        }
    }

    /**
     * Logs messages with the most outer class name of the code that is calling this method.
     */
    public static void printException(LogMessage message) {
        Log.e("revanced: " + message.findOuterClassSimpleName(), message.buildMessageString());
    }

    /**
     * Logs exceptions with the most outer class name of the code that is calling this method.
     */
    public static void printException(LogMessage message, Throwable ex) {
        Log.e("revanced: " + message.findOuterClassSimpleName(), message.buildMessageString(), ex);
    }

    /**
     * Deprecated. Instead call {@link #printDebug(LogMessage)},
     * which does not cause log messages to be constructed unless logging is enabled.
     */
    @Deprecated
    public static void debug(Class _clazz, String message) {
        printDebug(() -> message); // this fails to show the correct calling class name, but it's deprecated who cares
    }

    /**
     * Deprecated.  Instead call {@link #printException(LogMessage, Throwable)}
     * or {@link #printException(LogMessage)}
     * which does not cause log messages to be constructed unless logging is enabled.
     */
    @Deprecated
    public static void printException(Class _clazz, String message, Throwable ex) {
        printException(() -> message, ex);
    }

    /**
     * Deprecated. Instead call {@link #printException(LogMessage)},
     * which does not cause log messages to be constructed unless logging is enabled.
     */
    @Deprecated
    public static void printException(Class _clazz, String message) {
        printException(() -> message);
    }

    /**
     * Deprecated. Instead call {@link #printInfo(LogMessage)},
     * which does not cause log messages to be constructed unless logging is enabled.
     */
    @Deprecated
    public static void info(Class _clazz, String message) {
        printInfo(() -> message);
    }
}