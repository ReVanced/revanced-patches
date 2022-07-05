package app.revanced.integrations.ryd.dialog;

import static app.revanced.integrations.sponsorblock.StringRef.str;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.LightingColorFilter;
import android.net.Uri;
import android.os.Build;

import app.revanced.integrations.settings.SettingsEnum;
import app.revanced.integrations.utils.ReVancedUtils;

public class Dialogs {
    // Inject call from YT to this
    public static void showDialogsAtStartup(Activity activity) {
        rydFirstRun(activity);
    }

    private static void rydFirstRun(Activity activity) {
        boolean enabled = SettingsEnum.RYD_ENABLED_BOOLEAN.getBoolean();
        boolean hintShown = SettingsEnum.RYD_HINT_SHOWN_BOOLEAN.getBoolean();

        // If RYD is enabled or hint has been shown, exit
        if (enabled || hintShown) {
            // If RYD is enabled but hint hasn't been shown, mark it as shown
            if (enabled && !hintShown) {
                SettingsEnum.RYD_HINT_SHOWN_BOOLEAN.saveValue(true);
            }
            return;
        }

        AlertDialog.Builder builder;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            builder = new AlertDialog.Builder(activity, android.R.style.Theme_Material_Dialog_Alert);
        } else {
            builder = new AlertDialog.Builder(activity);
        }
        builder.setTitle(str("revanced_ryd"));
        builder.setIcon(ReVancedUtils.getIdentifier("reel_dislike_icon", "drawable"));
        builder.setCancelable(false);
        builder.setMessage(str("revanced_ryd_firstrun"));
        builder.setPositiveButton(str("revanced_enable"),
                (dialog, id) -> {
                    SettingsEnum.RYD_HINT_SHOWN_BOOLEAN.saveValue(true);
                    SettingsEnum.RYD_ENABLED_BOOLEAN.saveValue(true);
                    dialog.dismiss();
                });

        builder.setNegativeButton(str("revanced_disable"),
                (dialog, id) -> {
                    SettingsEnum.RYD_HINT_SHOWN_BOOLEAN.saveValue(true);
                    SettingsEnum.RYD_ENABLED_BOOLEAN.saveValue(false);
                    dialog.dismiss();
                });

        builder.setNeutralButton(str("revanced_learnmore"), null);

        AlertDialog dialog = builder.create();
        dialog.show();

        // Set black background
        dialog.getWindow().getDecorView().getBackground().setColorFilter(new LightingColorFilter(0xFF000000, ReVancedUtils.getIdentifier("ytBrandBackgroundSolid", "color")));

        // Set learn more action (set here so clicking it doesn't dismiss the dialog)
        dialog.getButton(DialogInterface.BUTTON_NEUTRAL).setOnClickListener(v -> {
            Uri uri = Uri.parse("https://www.returnyoutubedislike.com/");
            Intent intent = new Intent(Intent.ACTION_VIEW, uri);
            activity.startActivity(intent);
        });
    }
}
