package app.revanced.extension.samsung.radio.restrictions.device;

import android.os.SemSystemProperties;

import java.util.Arrays;

@SuppressWarnings("unused")
public final class BypassDeviceChecksPatch {

    /**
     * Injection point.
     * <p>
     * Check if the device has the required hardware
     **/
    public static final boolean checkIfDeviceIsIncompatible(String[] deviceList) {
        String currentDevice = SemSystemProperties.getSalesCode();
        return Arrays.asList(deviceList).contains(currentDevice);
    }
}
