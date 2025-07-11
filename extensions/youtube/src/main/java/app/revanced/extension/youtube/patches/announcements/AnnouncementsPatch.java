package app.revanced.extension.youtube.patches.announcements;

import static android.text.Html.FROM_HTML_MODE_COMPACT;
import static app.revanced.extension.shared.StringRef.str;
import static app.revanced.extension.shared.Utils.dipToPixels;
import static app.revanced.extension.youtube.patches.announcements.requests.AnnouncementsRoutes.GET_LATEST_ANNOUNCEMENTS;
import static app.revanced.extension.youtube.patches.announcements.requests.AnnouncementsRoutes.GET_LATEST_ANNOUNCEMENT_IDS;

import android.app.Activity;
import android.app.Dialog;
import android.text.Html;
import android.util.Pair;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.json.JSONArray;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.time.LocalDateTime;

import app.revanced.extension.shared.Logger;
import app.revanced.extension.shared.Utils;
import app.revanced.extension.shared.requests.Requester;
import app.revanced.extension.youtube.patches.announcements.requests.AnnouncementsRoutes;
import app.revanced.extension.youtube.settings.Settings;

@SuppressWarnings("unused")
public final class AnnouncementsPatch {
    private AnnouncementsPatch() {
    }

    private static boolean isLatestAlready() throws IOException {
        HttpURLConnection connection =
                AnnouncementsRoutes.getAnnouncementsConnectionFromRoute(GET_LATEST_ANNOUNCEMENT_IDS);

        Logger.printDebug(() -> "Get latest announcement IDs route connection url: " + connection.getURL());

        try {
            // Do not show the announcement if the request failed.
            if (connection.getResponseCode() != 200) {
                if (Settings.ANNOUNCEMENT_LAST_ID.isSetToDefault())
                    return true;

                Settings.ANNOUNCEMENT_LAST_ID.resetToDefault();
                Utils.showToastLong(str("revanced_announcements_connection_failed"));

                return true;
            }
        } catch (IOException ex) {
            Logger.printException(() -> "Could not connect to announcements provider", ex);
            return true;
        }

        var jsonString = Requester.parseStringAndDisconnect(connection);

        // Parse the ID. Fall-back to raw string if it fails.
        int id = Settings.ANNOUNCEMENT_LAST_ID.defaultValue;
        try {
            final var announcementIds = new JSONArray(jsonString);
            if (announcementIds.length() == 0) return true;
            
            id = announcementIds.getJSONObject(0).getInt("id");
        } catch (Throwable ex) {
            Logger.printException(() -> "Failed to parse announcement ID", ex);
        }

        // Do not show the announcement, if the last announcement id is the same as the current one.
        return Settings.ANNOUNCEMENT_LAST_ID.get() == id;
    }

    public static void showAnnouncement(final Activity context) {
        if (!Settings.ANNOUNCEMENTS.get()) return;

        // Check if there is internet connection
        if (!Utils.isNetworkConnected()) return;

        Utils.runOnBackgroundThread(() -> {
            try {
                if (isLatestAlready()) return;

                HttpURLConnection connection = AnnouncementsRoutes
                        .getAnnouncementsConnectionFromRoute(GET_LATEST_ANNOUNCEMENTS);

                Logger.printDebug(() -> "Get latest announcements route connection url: " + connection.getURL());

                var jsonString = Requester.parseStringAndDisconnect(connection);

                // Parse the announcement. Fall-back to raw string if it fails.
                int id = Settings.ANNOUNCEMENT_LAST_ID.defaultValue;
                String title;
                String message;
                LocalDateTime archivedAt = LocalDateTime.MAX;
                Level level = Level.INFO;
                try {
                    final var announcement = new JSONArray(jsonString).getJSONObject(0);

                    id = announcement.getInt("id");
                    title = announcement.getString("title");
                    message = announcement.getString("content");
                    if (!announcement.isNull("archived_at")) {
                        archivedAt = LocalDateTime.parse(announcement.getString("archived_at"));
                    }
                    if (!announcement.isNull("level")) {
                        level = Level.fromInt(announcement.getInt("level"));
                    }
                } catch (Throwable ex) {
                    Logger.printException(() -> "Failed to parse announcement. Fall-backing to raw string", ex);

                    title = "Announcement";
                    message = jsonString;
                }

                // If the announcement is archived, do not show it.
                if (archivedAt.isBefore(LocalDateTime.now())) {
                    Settings.ANNOUNCEMENT_LAST_ID.save(id);
                    return;
                }

                int finalId = id;
                final var finalTitle = title;
                final var finalMessage = Html.fromHtml(message, FROM_HTML_MODE_COMPACT);
                final Level finalLevel = level;

                Utils.runOnMainThread(() -> {
                    // Create the custom dialog and show the announcement.
                    Pair<Dialog, LinearLayout> dialogPair = Utils.createCustomDialog(
                            context,
                            finalTitle,                           // Title.
                            finalMessage,                         // Message.
                            null,                                 // No EditText.
                            null,                                 // OK button text.
                            () -> Settings.ANNOUNCEMENT_LAST_ID.save(finalId), // OK button action.
                            () -> {},                             // Cancel button action (dismiss only).
                            str("revanced_announcements_dialog_dismiss"), // Neutral button text.
                            () -> {},                             // Neutral button action (dismiss only).
                            true                                  // Dismiss dialog when onNeutralClick.
                    );

                    Dialog dialog = dialogPair.first;
                    LinearLayout mainLayout = dialogPair.second;

                    // Set the icon for the title TextView
                    for (int i = 0, childCould = mainLayout.getChildCount(); i < childCould; i++) {
                        View child = mainLayout.getChildAt(i);
                        if (child instanceof TextView childTextView && finalTitle.equals(childTextView.getText().toString())) {
                            childTextView.setCompoundDrawablesWithIntrinsicBounds(
                                    finalLevel.icon, 0, 0, 0);
                            childTextView.setCompoundDrawablePadding(dipToPixels(8));
                        }
                    }

                    // Set dialog as non-cancelable.
                    dialog.setCancelable(false);

                    // Show the dialog.
                    Utils.showDialog(context, dialog);
                });
            } catch (Exception e) {
                final var message = "Failed to get announcement";

                Logger.printException(() -> message, e);
            }
        });
    }

    // TODO: Use better icons.
    private enum Level {
        INFO(android.R.drawable.ic_dialog_info),
        WARNING(android.R.drawable.ic_dialog_alert),
        SEVERE(android.R.drawable.ic_dialog_alert);

        public final int icon;

        Level(int icon) {
            this.icon = icon;
        }

        public static Level fromInt(int value) {
            return values()[Math.min(value, values().length - 1)];
        }
    }
}
