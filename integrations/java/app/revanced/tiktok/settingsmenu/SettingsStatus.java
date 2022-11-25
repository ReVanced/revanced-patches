package app.revanced.tiktok.settingsmenu;

public class SettingsStatus {
    public static boolean feedFilter = false;
    public static boolean download = false;
    public static boolean simSpoof = false;

    public static void enableFeedFilter() {
        feedFilter = true;
    }

    public static void enableDownload() {
        download = true;
    }

    public static void enableSimSpoof() {
        simSpoof = true;
    }

    public static void load() {

    }
}
