package app.revanced.extension.samsung.radio.restrictions.device;

import app.revanced.extension.shared.Utils;

import android.os.SemSystemProperties;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("unused")
public final class BypassDeviceChecksPatch {

    /**
     * Injection point.
     * <p>
     * Check if the device has the required hardware
     **/
    public static final boolean checkIfDeviceIsIncompatible(String[] deviceList) {
        String currentDevice = SemSystemProperties.getSalesCode();
        return Utils.arrayContains(deviceList, currentDevice);
    }
}
