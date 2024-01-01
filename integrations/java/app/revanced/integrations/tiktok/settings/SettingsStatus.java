package app.revanced.integrations.tiktok.settings;

public class SettingsStatus {
    public static boolean feedFilterEnabled = false;
    public static boolean downloadEnabled = false;
    public static boolean simSpoofEnabled = false;

    public static void enableFeedFilter() {
        feedFilterEnabled = true;
    }

    public static void enableDownload() {
        downloadEnabled = true;
    }

    public static void enableSimSpoof() {
        simSpoofEnabled = true;
    }

    public static void load() {

    }
}
