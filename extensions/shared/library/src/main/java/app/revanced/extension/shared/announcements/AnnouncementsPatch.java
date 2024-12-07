package app.revanced.extension.shared.announcements;

import static android.text.Html.FROM_HTML_MODE_COMPACT;
import static app.revanced.extension.shared.StringRef.str;
import static app.revanced.extension.shared.announcements.requests.AnnouncementsRoutes.GET_LATEST_ANNOUNCEMENTS;
import static app.revanced.extension.shared.announcements.requests.AnnouncementsRoutes.GET_LATEST_ANNOUNCEMENT_IDS;

import android.app.Activity;
import android.app.AlertDialog;
import android.os.Build;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.widget.TextView;

import androidx.annotation.RequiresApi;

import org.json.JSONArray;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.time.LocalDateTime;

import app.revanced.extension.shared.Logger;
import app.revanced.extension.shared.Utils;
import app.revanced.extension.shared.requests.Requester;
import app.revanced.extension.shared.announcements.requests.AnnouncementsRoutes;
import app.revanced.extension.shared.settings.BaseSettings;

@SuppressWarnings("unused")
public final class AnnouncementsPatch {
    private AnnouncementsPatch() {
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private static boolean isLatestAlready() throws IOException {
        HttpURLConnection connection =
                AnnouncementsRoutes.getAnnouncementsConnectionFromRoute(GET_LATEST_ANNOUNCEMENT_IDS);

        Logger.printDebug(() -> "Get latest announcement IDs route connection url: " + connection.getURL());

        try {
            // Do not show the announcement if the request failed.
            if (connection.getResponseCode() != 200) {
                if (BaseSettings.ANNOUNCEMENT_LAST_ID.isSetToDefault())
                    return true;

                BaseSettings.ANNOUNCEMENT_LAST_ID.resetToDefault();
                Utils.showToastLong(str("revanced_announcements_connection_failed"));

                return true;
            }
        } catch (IOException ex) {
            Logger.printException(() -> "Could not connect to announcements provider", ex);
            return true;
        }

        var jsonString = Requester.parseStringAndDisconnect(connection);

        // Parse the ID. Fall-back to raw string if it fails.
        int id = BaseSettings.ANNOUNCEMENT_LAST_ID.defaultValue;
        try {
            final var announcementIds = new JSONArray(jsonString);
            id = announcementIds.getJSONObject(0).getInt("id");

        } catch (Throwable ex) {
            Logger.printException(() -> "Failed to parse announcement IDs", ex);
        }

        // Do not show the announcement, if the last announcement id is the same as the current one.
        return BaseSettings.ANNOUNCEMENT_LAST_ID.get() == id;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public static void showAnnouncement(final Activity context) {
        if (!BaseSettings.ANNOUNCEMENTS.get()) return;

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
                int id = BaseSettings.ANNOUNCEMENT_LAST_ID.defaultValue;
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
                    BaseSettings.ANNOUNCEMENT_LAST_ID.save(id);
                    return;
                }

                int finalId = id;
                final var finalTitle = title;
                final var finalMessage = Html.fromHtml(message, FROM_HTML_MODE_COMPACT);
                final Level finalLevel = level;

                Utils.runOnMainThread(() -> {
                    // Show the announcement.
                    var alert = new AlertDialog.Builder(context)
                            .setTitle(finalTitle)
                            .setMessage(finalMessage)
                            .setIcon(finalLevel.icon)
                            .setPositiveButton(android.R.string.ok, (dialog, which) -> {
                                BaseSettings.ANNOUNCEMENT_LAST_ID.save(finalId);
                                dialog.dismiss();
                            }).setNegativeButton(str("revanced_announcements_dialog_dismiss"), (dialog, which) -> {
                                dialog.dismiss();
                            })
                            .setCancelable(false)
                            .create();

                    Utils.showDialog(context, alert, false, (AlertDialog dialog) -> {
                        // Make links clickable.
                        ((TextView) dialog.findViewById(android.R.id.message))
                                .setMovementMethod(LinkMovementMethod.getInstance());
                    });
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
