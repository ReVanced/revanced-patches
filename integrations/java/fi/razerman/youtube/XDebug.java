package fi.razerman.youtube;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.graphics.Point;
import android.graphics.Rect;
import android.media.MediaCodec;
import android.os.Build;
import android.util.Base64;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;

import com.google.android.apps.youtube.app.YouTubeTikTokRoot_Application;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

/* loaded from: classes6.dex */
public class XDebug {
    private static final char[] hexArray = "0123456789ABCDEF".toCharArray();

    public static void printBooleanWithMethod(boolean bool) {
        StackTraceElement[] stackTraceElements = Thread.currentThread().getStackTrace();
        if (stackTraceElements.length <= 3) {
            Log.d("XBoolean", "Couldn't locate the method called from.");
            Log.d("XBoolean", "" + bool);
            return;
        }
        Log.d("XBoolean", "Called from method: " + stackTraceElements[3].toString() + "\n");
        Log.d("XBoolean", "" + bool);
    }

    public static void printColorStateListWithMethod(ColorStateList colorStateList) {
        StackTraceElement[] stackTraceElements = Thread.currentThread().getStackTrace();
        if (stackTraceElements.length <= 3) {
            Log.d("XColorStateList", "Couldn't locate the method called from.");
        } else {
            Log.d("XColorStateList", "Called from method: " + stackTraceElements[3].toString() + "\n");
        }
        if (colorStateList == null) {
            Log.d("XColorStateList", "<Null>");
        } else {
            Log.d("XColorStateList", "" + colorStateList);
        }
    }

    public static void printIntIntWithMethod(int integer, int integer2) {
        StackTraceElement[] stackTraceElements = Thread.currentThread().getStackTrace();
        if (stackTraceElements.length <= 3) {
            Log.d("XIntInt", "Couldn't locate the method called from.");
            Log.d("XIntInt", "" + integer + " | " + integer2);
            return;
        }
        Log.d("XIntInt", "Called from method: " + stackTraceElements[3].toString() + "\n");
        Log.d("XIntInt", "" + integer + " | " + integer2);
    }

    public static void printIntWithMethod(int integer) {
        StackTraceElement[] stackTraceElements = Thread.currentThread().getStackTrace();
        if (stackTraceElements.length <= 3) {
            Log.d("XInt", "Couldn't locate the method called from.");
            Log.d("XInt", "" + integer);
            return;
        }
        Log.d("XInt", "Called from method: " + stackTraceElements[3].toString() + "\n");
        Log.d("XInt", "" + integer);
    }

    public static void printStringWithMethod(String string) {
        if (string == null || string.isEmpty()) {
            string = "-- null --";
        }
        StackTraceElement[] stackTraceElements = Thread.currentThread().getStackTrace();
        if (stackTraceElements.length <= 3) {
            Log.d("XString", "Couldn't locate the method called from.");
            Log.d("XString", string);
            return;
        }
        Log.d("XString", "Called from method: " + stackTraceElements[3].toString() + "\n");
        Log.d("XString", string);
    }

    public static void printCharSequenceBooleanWithMethod(CharSequence charSequence, boolean bool) {
        StackTraceElement[] stackTraceElements = Thread.currentThread().getStackTrace();
        if (stackTraceElements.length <= 3) {
            Log.d("XCharSequenceB", "Couldn't locate the method called from.");
        } else {
            Log.d("XCharSequenceB", "Called from method: " + stackTraceElements[3].toString() + "\n");
        }
        if (charSequence == null) {
            Log.d("XCharSequenceB", "<Null>");
        } else {
            Log.d("XCharSequenceB", charSequence + " | " + (bool ? "true" : "false"));
        }
    }

    public static void printCharSequenceWithMethod(CharSequence charSequence) {
        StackTraceElement[] stackTraceElements = Thread.currentThread().getStackTrace();
        if (stackTraceElements.length <= 3) {
            Log.d("XCharSequence", "Couldn't locate the method called from.");
        } else {
            Log.d("XCharSequence", "Called from method: " + stackTraceElements[3].toString() + "\n");
        }
        if (charSequence == null) {
            Log.d("XCharSequence", "<Null>");
        } else {
            Log.d("XCharSequence", charSequence.toString());
        }
    }

    public static void printCharSequenceAndBufferTypeWithMethod(CharSequence charSequence, TextView.BufferType bufferType) {
        StackTraceElement[] stackTraceElements = Thread.currentThread().getStackTrace();
        if (stackTraceElements.length <= 3) {
            Log.d("XCharSequenceBT", "Couldn't locate the method called from.");
            if (charSequence == null) {
                if (bufferType == null) {
                    Log.d("XCharSequenceBT", "<Null>");
                } else {
                    Log.d("XCharSequenceBT", "<Null> | " + bufferType);
                }
            } else if (bufferType == null) {
                Log.d("XCharSequenceBT", charSequence.toString());
            } else {
                Log.d("XCharSequenceBT", charSequence.toString() + " | " + bufferType);
            }
        } else {
            Log.d("XCharSequenceBT", "Called from method: " + stackTraceElements[3].toString() + "\n");
            if (charSequence == null) {
                Log.d("XCharSequenceBT", "<Null>");
            } else {
                Log.d("XCharSequenceBT", charSequence.toString());
            }
        }
    }

    public static void printStringBuilder(StringBuilder stringBuilder) {
        StackTraceElement[] stackTraceElements = Thread.currentThread().getStackTrace();
        if (stackTraceElements.length <= 3) {
            Log.d("XStringBuilder", "Couldn't locate the method called from.");
            Log.d("XStringBuilder", stringBuilder.toString());
            return;
        }
        Log.d("XStringBuilder", "Called from method: " + stackTraceElements[3].toString() + "\n");
        Log.d("XStringBuilder", stringBuilder.toString());
        Log.d("StackWithMethod", stringBuilder.toString());
        printStackTrace("StackWithMethod");
    }

    public static void printMethod() {
        StackTraceElement[] stackTraceElements = Thread.currentThread().getStackTrace();
        if (stackTraceElements.length > 3) {
            Log.d("XStack", stackTraceElements[3].toString() + "\n");
        }
    }

    public static void printStackTraces() {
        StackTraceElement[] stackTraceElements = Thread.currentThread().getStackTrace();
        for (StackTraceElement element : stackTraceElements) {
            System.out.println("Class name :: " + element.getClassName() + "  || method name :: " + element.getMethodName());
        }
    }

    public static void printStackTrace() {
        StackTraceElement[] stackTraceElements = Thread.currentThread().getStackTrace();
        StringBuilder stringBuilder = new StringBuilder();
        for (StackTraceElement element : stackTraceElements) {
            stringBuilder.append(element.toString()).append("\n");
        }
        Log.d("xfileSTACK", stringBuilder.toString());
    }

    public static void printStackTrace(String tag) {
        StackTraceElement[] stackTraceElements = Thread.currentThread().getStackTrace();
        StringBuilder stringBuilder = new StringBuilder();
        for (StackTraceElement element : stackTraceElements) {
            stringBuilder.append(element.toString()).append("\n");
        }
        Log.d(tag, stringBuilder.toString());
    }

    public static void printDebugBoolean(boolean val) {
        Log.d("XDebug", "" + val);
    }

    public static void printDebugInteger(int value) {
        Log.d("XDebug", "" + value);
    }

    public static void printDebugFloat(float value) {
        Log.d("XDebug", "" + value);
    }

    public static void printDebugLong(long value) {
        Log.d("XDebug", "" + value);
    }

    public static void printDebugString(String value) {
        if (value != null) {
            Log.d("XDebug", value);
        }
    }

    public static void printDebugStringWithMethodName(String value) {
        StackTraceElement[] stackTraceElements;
        if (value != null && (stackTraceElements = Thread.currentThread().getStackTrace()) != null && stackTraceElements.length > 3) {
            Log.d("XDebug", value + " | " + stackTraceElements[3].toString() + "\n");
        }
    }

    public static void printDebugStringWithStack(String value) {
        StackTraceElement[] stackTraceElements;
        if (!(value == null || (stackTraceElements = Thread.currentThread().getStackTrace()) == null)) {
            StringBuilder stringBuilder = new StringBuilder();
            for (StackTraceElement element : stackTraceElements) {
                stringBuilder.append(element.toString()).append("\n");
            }
            Log.d("XDebug", value + " | " + stringBuilder.toString());
        }
    }

    public static void printDebugByteArray(byte[] value) {
        Log.d("XDebug", bytesToHex(value));
    }

    public static void printByteBufferWithMethod(ByteBuffer buf) {
        String string;
        if (buf == null) {
            string = "-- null --";
        } else {
            string = new String(buf.array(), StandardCharsets.UTF_8);
            if (string.isEmpty()) {
                string = "-- null --";
            }
        }
        StackTraceElement[] stackTraceElements = Thread.currentThread().getStackTrace();
        if (stackTraceElements.length <= 3) {
            Log.d("XByteBuffer", "Couldn't locate the method called from.");
            Log.d("XByteBuffer", string);
            return;
        }
        Log.d("XByteBuffer", "Called from method: " + stackTraceElements[3].toString() + "\n");
        Log.d("XByteBuffer", string);
    }

    public static void printDebugByteBuffer(ByteBuffer buf) {
        byte[] bytes = new byte[buf.remaining()];
        buf.get(bytes, 0, bytes.length);
        buf.clear();
        byte[] bytes2 = new byte[buf.capacity()];
        buf.get(bytes2, 0, bytes2.length);
        Log.d("XDebug", bytesToHex(bytes2));
    }

    public static void printDebugByteBuffer(ByteBuffer[] buffers) {
        int length = buffers.length;
        int i = 0;
        int index = 0;
        while (i < length) {
            ByteBuffer buf = buffers[i];
            byte[] bytes = new byte[buf.remaining()];
            buf.get(bytes, 0, bytes.length);
            buf.clear();
            byte[] bytes2 = new byte[buf.capacity()];
            buf.get(bytes2, 0, bytes2.length);
            Log.d("XDebug - Index: " + index, bytesToHex(bytes2));
            i++;
            index++;
        }
    }

    public static void printDebugMediaCodec(MediaCodec mediaCodec) {
        Exception e;
        int i = 0;
        try {
            ByteBuffer[] outputBuffers = mediaCodec.getOutputBuffers();
            int length = outputBuffers.length;
            int index = 0;
            while (i < length) {
                try {
                    ByteBuffer buf = outputBuffers[i];
                    byte[] bytes = new byte[buf.remaining()];
                    buf.get(bytes, 0, bytes.length);
                    buf.clear();
                    byte[] bytes2 = new byte[buf.capacity()];
                    buf.get(bytes2, 0, bytes2.length);
                    int index2 = index + 1;
                    Log.d("XDebug - Index: " + index, bytesToHex(bytes2));
                    i++;
                    index = index2;
                } catch (Exception e2) {
                    e = e2;
                    Log.d("XDebug abc", "Error: " + e.getMessage());
                    return;
                }
            }
        } catch (Exception ignored) {
        }
    }

    public static void printDebugMediaCodec(MediaCodec mediaCodec, int i) {
        try {
            ByteBuffer buf = mediaCodec.getOutputBuffer(i);
            byte[] bytes = getByteArrayFromByteBuffer(buf);
            Log.d("XDebug - decrypt: " + i, bytesToHex(bytes));
        } catch (Exception e) {
            Log.d("XDebug - buffer: " + i, "Error: " + i + " | " + e.getMessage());
        }
    }

    private static byte[] getByteArrayFromByteBuffer(ByteBuffer byteBuffer) {
        byte[] bytesArray = new byte[byteBuffer.capacity()];
        byteBuffer.get(bytesArray, 0, bytesArray.length);
        return bytesArray;
    }

    public static void printDebugRect(Rect value) {
        Log.d("XDebug", "Rectangle| Left:" + value.left + " - Top: " + value.top + " - Right: " + value.right + " - Bottom: " + value.bottom);
    }

    public static DisplayMetrics getMetrics() {
        return new DisplayMetrics();
    }

    public static Point getRealSize() {
        return new Point(3840, 2160);
    }

    public static Point getSize() {
        return new Point(3840, 2160);
    }

    public static Point getPhysicalSize() {
        return new Point(3840, 2160);
    }

    public static int getDisplayWidth() {
        return 2160;
    }

    public static int getDisplayHeight() {
        return 3840;
    }

    public static String CheckYTRed(String input, String original) {
        if (input.equals("has_unlimited_entitlement") || input.equals("has_unlimited_ncc_free_trial")) {
            return "True";
        }
        if (input.equals("e")) {
            return "11202604,23700636,23701019,9415293,9422596,9431754,9435797,9444109,9444635,9449243,9456940,9461315,9463829,9464088,9466234,9467503,9474594,9476327,9477602,9478523,9479785,9480475,9480495,9482942,9484378,9484706,9488038,9489706";
        }
        return original;
    }

    public static String DecodeColdConfig() {
        Context context = YouTubeTikTokRoot_Application.getAppContext();
        if (XGlobals.XFILEDEBUG && context == null) {
            context = XSettingActivity.getAppContext();
        }
        if (context == null) {
            Log.e("XDebug", "Context is null, ignoring to decode");
            return Build.MANUFACTURER;
        }
        SharedPreferences sharedPreferences = context.getSharedPreferences("youtube", 0);
        String config_group = sharedPreferences.getString("com.google.android.libraries.youtube.innertube.cold_config_group", null);
        String decoded = "";
        if (config_group == null) {
            return decoded;
        }
        try {
            if (config_group.isEmpty()) {
                return decoded;
            }
            decoded = bytesToHex(Base64.decode(config_group, 8));
            sharedPreferences.edit().putString("com.google.android.libraries.youtube.innertube.cold_config_group.decoded", decoded).apply();
            return decoded;
        } catch (Exception e) {
            return decoded;
        }
    }

    public static String bytesToHex(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        for (int j = 0; j < bytes.length; j++) {
            int v = bytes[j] & 255;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[(j * 2) + 1] = hexArray[v & 15];
        }
        return new String(hexChars);
    }

    public static long ConvertDoubleToLong(double value) {
        return (long) value;
    }

    public static void getViewHierarchy(@NonNull View v) {
        StringBuilder desc = new StringBuilder();
        getViewHierarchy(v, desc, 0);
        Log.d("XDebug", desc.toString());
    }

    private static void getViewHierarchy(View v, StringBuilder desc, int margin) {
        desc.append(getViewMessage(v, margin));
        if (v instanceof ViewGroup) {
            int margin2 = margin + 1;
            ViewGroup vg = (ViewGroup) v;
            for (int i = 0; i < vg.getChildCount(); i++) {
                getViewHierarchy(vg.getChildAt(i), desc, margin2);
            }
        }
    }

    private static String getViewMessage(View v, int marginOffset) {
        String resourceId;
        String repeated = new String(new char[marginOffset]).replace("\u0000", "  ");
        try {
            if (v.getResources() != null) {
                resourceId = v.getId() != 0 ? v.getResources().getResourceName(v.getId()) : "no_id";
            } else {
                resourceId = "no_resources";
            }
            return repeated + "[" + v.getClass().getSimpleName() + "] " + resourceId + "\n";
        } catch (Resources.NotFoundException e) {
            return repeated + "[" + v.getClass().getSimpleName() + "] name_not_found\n";
        }
    }
}
