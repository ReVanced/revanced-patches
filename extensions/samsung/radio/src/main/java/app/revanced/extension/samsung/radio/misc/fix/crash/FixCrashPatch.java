package app.revanced.extension.samsung.radio.misc.fix.crash;

import app.revanced.extension.shared.Utils;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("unused")
public final class FixCrashPatch {

    /**
     * Injection point.
     * <p>
     * Add the required permissions to the request list to avoid crashes on API 34+.
     **/
    public static final String[] fixPermissionRequestList(String[] perms) {
        if (Utils.arrayContains(perms, "android.permission.POST_NOTIFICATIONS")) {
            perms = (String[]) Utils.mergeArrays(perms, new String[]{"android.permission.RECORD_AUDIO", "android.permission.READ_PHONE_STATE", "android.permission.FOREGROUND_SERVICE_MICROPHONE"});
        }
        if (Utils.arrayContains(perms, "android.permission.RECORD_AUDIO")) {
            perms = (String[]) Utils.mergeArrays(perms, new String[]{"android.permission.FOREGROUND_SERVICE_MICROPHONE"});
        }
        return perms;
    }
}
