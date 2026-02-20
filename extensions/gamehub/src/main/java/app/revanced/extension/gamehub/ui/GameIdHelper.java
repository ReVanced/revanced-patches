package app.revanced.extension.gamehub.ui;

import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Displays copyable game IDs in the game detail screen.
 * Each ID is a separate TextView that copies its value on tap.
 */
@SuppressWarnings("unused")
public class GameIdHelper {

    public static void populateGameId(Activity activity) {
        try {
            Bundle extras = activity.getIntent().getExtras();
            if (extras == null) return;

            String steamAppId = extras.getString("steamAppId", "");
            String localGameId = extras.getString("localGameId", "");

            boolean hasSteam = !steamAppId.isEmpty() && !"0".equals(steamAppId);
            boolean hasLocal = !localGameId.isEmpty() && !"0".equals(localGameId);
            if (!hasSteam && !hasLocal) return;

            int containerId = resolveId(activity, "ll_game_id_container");
            if (containerId == 0) return;

            View container = activity.findViewById(containerId);
            if (container == null) return;

            container.setVisibility(View.VISIBLE);

            if (hasSteam) {
                setupCopyableText(activity, "tv_steam_app_id",
                        "Steam App ID: " + steamAppId, steamAppId, "Steam App ID");
            }
            if (hasLocal) {
                setupCopyableText(activity, "tv_local_game_id",
                        "Local Game ID: " + localGameId, localGameId, "Local Game ID");
            }
        } catch (Exception e) {
            // Silently ignore if layout elements are missing
        }
    }

    private static void setupCopyableText(Activity activity, String idName,
                                           String displayText, String copyValue, String label) {
        int id = resolveId(activity, idName);
        if (id == 0) return;

        TextView tv = activity.findViewById(id);
        if (tv == null) return;

        tv.setText(displayText);
        tv.setVisibility(View.VISIBLE);
        tv.setOnClickListener(v -> {
            ClipboardManager clipboard = (ClipboardManager)
                    activity.getSystemService(Context.CLIPBOARD_SERVICE);
            if (clipboard != null) {
                clipboard.setPrimaryClip(ClipData.newPlainText(label, copyValue));
                Toast.makeText(activity, label + " copied!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private static int resolveId(Activity activity, String name) {
        return activity.getResources().getIdentifier(name, "id", activity.getPackageName());
    }
}
