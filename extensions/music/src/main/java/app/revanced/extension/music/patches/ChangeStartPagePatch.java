package app.revanced.extension.music.patches;

import static java.lang.Boolean.TRUE;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import app.revanced.extension.shared.Logger;
import app.revanced.extension.music.settings.Settings;

@SuppressWarnings("unused")
public final class ChangeStartPagePatch {
    private static final String SHORTCUT_ACTION = "com.google.android.youtube.music.action.shortcut";
    private static final String SHORTCUT_CLASS_DESCRIPTOR = "com.google.android.apps.youtube.music.activities.InternalMusicActivity";
    private static final String SHORTCUT_TYPE = "com.google.android.youtube.music.action.shortcut_type";
    private static final String SHORTCUT_ID_SEARCH = "Eh4IBRDTnQEYmgMiEwiZn+H0r5WLAxVV5OcDHcHRBmPqpd25AQA=";
    private static final int SHORTCUT_TYPE_SEARCH = 1;


    public enum StartPage {
        DEFAULT("", null),
        CHARTS("FEmusic_charts", TRUE),
        EXPLORE("FEmusic_explore", TRUE),
        HISTORY("FEmusic_history", TRUE),
        LIBRARY("FEmusic_library_landing", TRUE),
        PLAYLISTS("FEmusic_liked_playlists", TRUE),
        PODCASTS("FEmusic_non_music_audio", TRUE),
        SUBSCRIPTIONS("FEmusic_library_corpus_artists", TRUE),
        EPISODES_FOR_LATER("VLSE", TRUE),
        LIKED_MUSIC("VLLM", TRUE),
        SEARCH("", false);

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
    }

    private static final String ACTION_MAIN = "android.intent.action.MAIN";

    public static String overrideBrowseId(@Nullable String original) {
        var startPage = Settings.CHANGE_START_PAGE.get();

        if (!startPage.isBrowseId()) {
            return original;
        }

        if (!"FEmusic_home".equals(original)) {
            return original;
        }

        String overrideBrowseId = startPage.id;
        if (overrideBrowseId.isEmpty()) {
            return original;
        }

        Logger.printDebug(() -> "Changing browseId to: " + startPage.name());
        return overrideBrowseId;
    }

    public static void overrideIntentActionOnCreate(@NonNull Activity activity,
                                                    @Nullable Bundle savedInstanceState) {
        if (savedInstanceState != null) return;

        var startPage = Settings.CHANGE_START_PAGE.get();
        if (startPage != StartPage.SEARCH) return;

        var originalIntent = activity.getIntent();
        if (originalIntent == null) return;

        if (ACTION_MAIN.equals(originalIntent.getAction())) {
            Logger.printDebug(() -> "Cold start: Launching search activity directly");
            var searchIntent = new Intent();

            searchIntent.setAction(SHORTCUT_ACTION);
            searchIntent.setClassName(activity, SHORTCUT_CLASS_DESCRIPTOR);
            searchIntent.setPackage(activity.getPackageName());
            searchIntent.putExtra(SHORTCUT_TYPE, SHORTCUT_TYPE_SEARCH);
            searchIntent.putExtra(SHORTCUT_ACTION, SHORTCUT_ID_SEARCH);

            activity.startActivity(searchIntent);
        }
    }
}