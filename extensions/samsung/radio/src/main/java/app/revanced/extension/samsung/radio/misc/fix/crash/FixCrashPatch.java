package app.revanced.extension.samsung.radio.misc.fix.crash;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@SuppressWarnings("unused")
public final class FixCrashPatch {
    /**
     * Injection point.
     * <p>
     * Add the required permissions to the request list to avoid crashes on API 34+.
     **/
    public static final String[] fixPermissionRequestList(String[] perms) {
        List<String> permsList = new ArrayList<>(Arrays.asList(perms));
        if (permsList.contains("android.permission.POST_NOTIFICATIONS")) {
            permsList.addAll(Arrays.asList("android.permission.RECORD_AUDIO", "android.permission.READ_PHONE_STATE", "android.permission.FOREGROUND_SERVICE_MICROPHONE"));
        }
        if (permsList.contains("android.permission.RECORD_AUDIO")) {
            permsList.add("android.permission.FOREGROUND_SERVICE_MICROPHONE");
        }
        return permsList.toArray(new String[0]);
    }
}
