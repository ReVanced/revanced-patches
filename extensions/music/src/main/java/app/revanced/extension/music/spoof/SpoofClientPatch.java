package app.revanced.extension.music.spoof;

/**
 * @noinspection unused
 */
public class SpoofClientPatch {
    private static final int CLIENT_TYPE_ID = 26;
    private static final String CLIENT_VERSION = "6.21";
    private static final String DEVICE_MODEL = "iPhone16,2";
    private static final String OS_VERSION = "17.7.2.21H221";

    public static int getClientId() {
        return CLIENT_TYPE_ID;
    }

    public static String getClientVersion() {
        return CLIENT_VERSION;
    }

    public static String getClientModel() {
        return DEVICE_MODEL;
    }

    public static String getOsVersion() {
        return OS_VERSION;
    }
}