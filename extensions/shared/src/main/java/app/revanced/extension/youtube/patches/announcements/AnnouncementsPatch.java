package app.revanced.extension.youtube.patches.announcements;

import android.app.Activity;
import android.os.Build;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.widget.TextView;
import androidx.annotation.RequiresApi;

import app.revanced.extension.shared.Logger;
import app.revanced.extension.shared.Utils;
import app.revanced.extension.youtube.patches.announcements.requests.AnnouncementsRoutes;
import app.revanced.extension.youtube.requests.Requester;
import app.revanced.extension.youtube.settings.Settings;
import org.json.JSONObject;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Locale;

import static android.text.Html.FROM_HTML_MODE_COMPACT;
import static app.revanced.extension.shared.StringRef.str;
import static app.revanced.extension.youtube.patches.announcements.requests.AnnouncementsRoutes.GET_LATEST_ANNOUNCEMENT;

@SuppressWarnings("unused")
public final class AnnouncementsPatch {
    private AnnouncementsPatch() {
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public static void showAnnouncement(final Activity context) {
        if (!Settings.ANNOUNCEMENTS.get()) return;

        // Check if there is internet connection
        if (!Utils.isNetworkConnected()) return;

        Utils.runOnBackgroundThread(() -> {
            try {
                HttpURLConnection connection = AnnouncementsRoutes.getAnnouncementsConnectionFromRoute(
                        GET_LATEST_ANNOUNCEMENT, Locale.getDefault().toLanguageTag());

                Logger.printDebug(() -> "Get latest announcement route connection url: " + connection.getURL());

                try {
                    // Do not show the announcement if the request failed.
                    if (connection.getResponseCode() != 200) {
                        if (Settings.ANNOUNCEMENT_LAST_ID.isSetToDefault())
                            return;

                        Settings.ANNOUNCEMENT_LAST_ID.resetToDefault();
                        Utils.showToastLong(str("revanced_announcements_connection_failed"));

                        return;
                    }
                } catch (IOException ex) {
                    final var message = "Failed connecting to announcements provider";

                    Logger.printException(() -> message, ex);
                    return;
                }

                var jsonString = Requester.parseStringAndDisconnect(connection);


                // Parse the announcement. Fall-back to raw string if it fails.
                int id = Settings.ANNOUNCEMENT_LAST_ID.defaultValue;
                String title;
                String message;
                Level level = Level.INFO;
                try {
                    final var announcement = new JSONObject(jsonString);

                    id = announcement.getInt("id");
                    title = announcement.getString("title");
                    message = announcement.getJSONObject("content").getString("message");
                    if (!announcement.isNull("level")) level = Level.fromInt(announcement.getInt("level"));

                } catch (Throwable ex) {
                    Logger.printException(() -> "Failed to parse announcement. Fall-backing to raw string", ex);

                    title = "Announcement";
                    message = jsonString;
                }

                // TODO: Remove this migration code after a few months.
                if (!Settings.DEPRECATED_ANNOUNCEMENT_LAST_HASH.isSetToDefault()){
                    final byte[] hashBytes = MessageDigest
                            .getInstance("SHA-256")
                            .digest(jsonString.getBytes(StandardCharsets.UTF_8));

                    final var hash = java.util.Base64.getEncoder().encodeToString(hashBytes);

                    // Migrate to saving the id instead of the hash.
                    if (hash.equals(Settings.DEPRECATED_ANNOUNCEMENT_LAST_HASH.get())) {
                        Settings.ANNOUNCEMENT_LAST_ID.save(id);
                    }

                    Settings.DEPRECATED_ANNOUNCEMENT_LAST_HASH.resetToDefault();
                }

                // Do not show the announcement, if the last announcement id is the same as the current one.
                if (Settings.ANNOUNCEMENT_LAST_ID.get() == id) return;



                int finalId = id;
                final var finalTitle = title;
                final var finalMessage = Html.fromHtml(message, FROM_HTML_MODE_COMPACT);
                final Level finalLevel = level;

                Utils.runOnMainThread(() -> {
                    // Show the announcement.
                    var alertDialog = new android.app.AlertDialog.Builder(context)
                            .setTitle(finalTitle)
                            .setMessage(finalMessage)
                            .setIcon(finalLevel.icon)
                            .setPositiveButton(android.R.string.ok, (dialog, which) -> {
                                Settings.ANNOUNCEMENT_LAST_ID.save(finalId);
                                dialog.dismiss();
                            }).setNegativeButton(str("revanced_announcements_dialog_dismiss"), (dialog, which) -> {
                                dialog.dismiss();
                            })
                            .setCancelable(false)
                            .show();

                    // Make links clickable.
                    ((TextView)alertDialog.findViewById(android.R.id.message))
                            .setMovementMethod(LinkMovementMethod.getInstance());
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
