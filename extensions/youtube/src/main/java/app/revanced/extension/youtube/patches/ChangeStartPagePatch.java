package app.revanced.extension.youtube.patches;

import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;

import android.content.Intent;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import app.revanced.extension.shared.Logger;
import app.revanced.extension.shared.settings.Setting;
import app.revanced.extension.youtube.settings.Settings;

import java.util.List;

@SuppressWarnings("unused")
public final class ChangeStartPagePatch {

    public enum StartPage {
        /**
         * Unmodified type, and same as un-patched.
         */
        DEFAULT("", null),

        /**
         * Browse id.
         */
        ALL_SUBSCRIPTIONS("FEchannels", TRUE),
        BROWSE("FEguide_builder", TRUE),
        EXPLORE("FEexplore", TRUE),
        HISTORY("FEhistory", TRUE),
        LIBRARY("FElibrary", TRUE),
        MOVIE("FEstorefront", TRUE),
        NOTIFICATIONS("FEactivity", TRUE),
        PLAYLISTS("FEplaylist_aggregation", TRUE),
        SUBSCRIPTIONS("FEsubscriptions", TRUE),
        TRENDING("FEtrending", TRUE),
        YOUR_CLIPS("FEclips", TRUE),

        /**
         * Channel id, this can be used as a browseId.
         */
        COURSES("UCtFRv9O2AHqOZjjynzrv-xg", TRUE),
        FASHION("UCrpQ4p1Ql_hG8rKXIKM1MOQ", TRUE),
        GAMING("UCOpNcN46UbXVtpKMrmU4Abg", TRUE),
        LIVE("UC4R8DWoMoI7CAwX8_LjQHig", TRUE),
        MUSIC("UC-9-kyTW8ZkZNDHQJ6FgpwQ", TRUE),
        NEWS("UCYfdidRxbB8Qhf0Nx7ioOYw", TRUE),
        SHOPPING("UCkYQyvc_i9hXEo4xic9Hh2g", TRUE),
        SPORTS("UCEgdi0XIXXZ-qJOFPf4JSKw", TRUE),
        VIRTUAL_REALITY("UCzuqhhs6NWbgTzMuM09WKDQ", TRUE),

        /**
         * Playlist id, this can be used as a browseId.
         */
        LIKED_VIDEO("VLLL", TRUE),
        WATCH_LATER("VLWL", TRUE),

        /**
         * Intent action.
         */
        SEARCH("com.google.android.youtube.action.open.search", FALSE),
        SHORTS("com.google.android.youtube.action.open.shorts", FALSE);

        @NonNull
        final String id;

        @Nullable
        final Boolean isBrowseId;

        StartPage(@NonNull String id, @Nullable Boolean isBrowseId) {
            this.id = id;
            this.isBrowseId = isBrowseId;
        }

        private boolean isBrowseId() {
            return TRUE.equals(isBrowseId);
        }

        @SuppressWarnings("BooleanMethodIsAlwaysInverted")
        private boolean isIntentAction() {
            return FALSE.equals(isBrowseId);
        }
    }

    public static class ChangeStartPageTypeAvailability implements Setting.Availability {
        @Override
        public boolean isAvailable() {
            return Settings.CHANGE_START_PAGE.get() != StartPage.DEFAULT;
        }

        @Override
        public List<Setting<?>> getParentSettings() {
            return List.of(Settings.CHANGE_START_PAGE);
        }
    }

    /**
     * Intent action when YouTube is cold started from the launcher.
     * <p>
     * If you don't check this, the hooking will also apply in the following cases:
     * Case 1. The user clicked Shorts button on the YouTube shortcut.
     * Case 2. The user clicked Shorts button on the YouTube widget.
     * In this case, instead of opening Shorts, the start page specified by the user is opened.
     */
    private static final String ACTION_MAIN = "android.intent.action.MAIN";

    private static final StartPage START_PAGE = Settings.CHANGE_START_PAGE.get();

    private static final boolean CHANGE_START_PAGE_ALWAYS = Settings.CHANGE_START_PAGE_ALWAYS.get();

    /**
     * There is an issue where the back button on the toolbar doesn't work properly.
     * As a workaround for this issue, instead of overriding the browserId multiple times, just override it once.
     */
    private static boolean appLaunched = false;

    public static String overrideBrowseId(@NonNull String original) {
        if (!START_PAGE.isBrowseId()) {
            return original;
        }

        if (!CHANGE_START_PAGE_ALWAYS && appLaunched) {
            Logger.printDebug(() -> "Ignore override browseId as the app already launched");
            return original;
        }
        appLaunched = true;

        Logger.printDebug(() -> "Changing browseId to: " + START_PAGE.id);
        return START_PAGE.id;
    }

    public static void overrideIntentAction(@NonNull Intent intent) {
        if (!START_PAGE.isIntentAction()) {
            return;
        }

        if (!ACTION_MAIN.equals(intent.getAction())) {
            Logger.printDebug(() -> "Ignore override intent action" +
                    " as the current activity is not the entry point of the application");
            return;
        }

        if (!CHANGE_START_PAGE_ALWAYS && appLaunched) {
            Logger.printDebug(() -> "Ignore override intent action as the app already launched");
            return;
        }
        appLaunched = true;

        String intentAction = START_PAGE.id;
        Logger.printDebug(() -> "Changing intent action to: " + intentAction);
        intent.setAction(intentAction);
    }
}
