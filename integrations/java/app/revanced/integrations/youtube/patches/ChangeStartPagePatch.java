package app.revanced.integrations.youtube.patches;

import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;

import android.content.Intent;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import app.revanced.integrations.shared.Logger;
import app.revanced.integrations.youtube.settings.Settings;

@SuppressWarnings("unused")
public final class ChangeStartPagePatch {

    public enum StartPage {
        /**
         * Unmodified type, and same as un-patched.
         */
        ORIGINAL("", null),

        /**
         * Browse id.
         */
        BROWSE("FEguide_builder", TRUE),
        EXPLORE("FEexplore", TRUE),
        HISTORY("FEhistory", TRUE),
        LIBRARY("FElibrary", TRUE),
        MOVIE("FEstorefront", TRUE),
        SUBSCRIPTIONS("FEsubscriptions", TRUE),
        TRENDING("FEtrending", TRUE),

        /**
         * Channel id, this can be used as a browseId.
         */
        GAMING("UCOpNcN46UbXVtpKMrmU4Abg", TRUE),
        LIVE("UC4R8DWoMoI7CAwX8_LjQHig", TRUE),
        MUSIC("UC-9-kyTW8ZkZNDHQJ6FgpwQ", TRUE),
        SPORTS("UCEgdi0XIXXZ-qJOFPf4JSKw", TRUE),

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

        @Nullable
        final Boolean isBrowseId;

        @NonNull
        final String id;

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

    /**
     * There is an issue where the back button on the toolbar doesn't work properly.
     * As a workaround for this issue, instead of overriding the browserId multiple times, just override it once.
     */
    private static boolean appLaunched = false;

    public static String overrideBrowseId(@NonNull String original) {
        if (!START_PAGE.isBrowseId()) {
            return original;
        }

        if (appLaunched) {
            Logger.printDebug(() -> "Ignore override browseId as the app already launched");
            return original;
        }
        appLaunched = true;

        Logger.printDebug(() -> "Changing browseId to " + START_PAGE.id);
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

        if (appLaunched) {
            Logger.printDebug(() -> "Ignore override intent action as the app already launched");
            return;
        }
        appLaunched = true;

        final String intentAction = START_PAGE.id;
        Logger.printDebug(() -> "Changing intent action to " + intentAction);
        intent.setAction(intentAction);
    }
}
