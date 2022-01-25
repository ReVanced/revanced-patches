package fi.vanced.libraries.youtube.dialog;

import static fi.vanced.libraries.youtube.ryd.RYDSettings.PREFERENCES_KEY_RYD_ENABLED;
import static fi.vanced.libraries.youtube.ryd.RYDSettings.PREFERENCES_KEY_RYD_HINT_SHOWN;
import static fi.vanced.libraries.youtube.ryd.RYDSettings.PREFERENCES_NAME;
import static pl.jakubweg.SponsorBlockSettings.PREFERENCES_KEY_SPONSOR_BLOCK_ENABLED;
import static pl.jakubweg.SponsorBlockSettings.PREFERENCES_KEY_SPONSOR_BLOCK_HINT_SHOWN;
import static pl.jakubweg.StringRef.str;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.LightingColorFilter;
import android.net.Uri;
import android.os.Build;

import com.google.android.apps.youtube.app.YouTubeTikTokRoot_Application;

import fi.vanced.utils.SharedPrefUtils;
import fi.vanced.utils.VancedUtils;
import pl.jakubweg.SponsorBlockSettings;

public class Dialogs {
    // Inject call from YT to this
    public static void showDialogsAtStartup(Activity activity) {
        rydFirstRun(activity);
        sbFirstRun(activity);
    }

    private static void rydFirstRun(Activity activity) {
        Context context = YouTubeTikTokRoot_Application.getAppContext();
        boolean enabled = SharedPrefUtils.getBoolean(context, PREFERENCES_NAME, PREFERENCES_KEY_RYD_ENABLED, false);
        boolean hintShown = SharedPrefUtils.getBoolean(context, PREFERENCES_NAME, PREFERENCES_KEY_RYD_HINT_SHOWN, false);

        // If RYD is enabled or hint has been shown, exit
        if (enabled || hintShown) {
            // If RYD is enabled but hint hasn't been shown, mark it as shown
            if (enabled && !hintShown) {
                SharedPrefUtils.saveBoolean(context, PREFERENCES_NAME, PREFERENCES_KEY_RYD_HINT_SHOWN, true);
            }
            return;
        }

        AlertDialog.Builder builder;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            builder = new AlertDialog.Builder(activity, android.R.style.Theme_Material_Dialog_Alert);
        } else {
            builder = new AlertDialog.Builder(activity);
        }
        builder.setTitle(str("vanced_ryd"));
        builder.setIcon(VancedUtils.getIdentifier("reel_dislike_icon", "drawable"));
        builder.setCancelable(false);
        builder.setMessage(str("vanced_ryd_firstrun"));
        builder.setPositiveButton(str("vanced_enable"),
                (dialog, id) -> {
                    SharedPrefUtils.saveBoolean(context, PREFERENCES_NAME, PREFERENCES_KEY_RYD_HINT_SHOWN, true);
                    SharedPrefUtils.saveBoolean(context, PREFERENCES_NAME, PREFERENCES_KEY_RYD_ENABLED, true);
                    dialog.dismiss();
                });

        builder.setNegativeButton(str("vanced_disable"),
                (dialog, id) -> {
                    SharedPrefUtils.saveBoolean(context, PREFERENCES_NAME, PREFERENCES_KEY_RYD_HINT_SHOWN, true);
                    SharedPrefUtils.saveBoolean(context, PREFERENCES_NAME, PREFERENCES_KEY_RYD_ENABLED, false);
                    dialog.dismiss();
                });

        builder.setNeutralButton(str("vanced_learnmore"), null);

        AlertDialog dialog = builder.create();
        dialog.show();

        // Set black background
        dialog.getWindow().getDecorView().getBackground().setColorFilter(new LightingColorFilter(0xFF000000, VancedUtils.getIdentifier("ytBrandBackgroundSolid", "color")));

        // Set learn more action (set here so clicking it doesn't dismiss the dialog)
        dialog.getButton(DialogInterface.BUTTON_NEUTRAL).setOnClickListener(v -> {
            Uri uri = Uri.parse("https://www.returnyoutubedislike.com/");
            Intent intent = new Intent(Intent.ACTION_VIEW, uri);
            activity.startActivity(intent);
        });
    }

    private static void sbFirstRun(Activity activity) {
        Context context = YouTubeTikTokRoot_Application.getAppContext();
        boolean enabled = SharedPrefUtils.getBoolean(context, SponsorBlockSettings.PREFERENCES_NAME, PREFERENCES_KEY_SPONSOR_BLOCK_ENABLED, false);
        boolean hintShown = SharedPrefUtils.getBoolean(context, SponsorBlockSettings.PREFERENCES_NAME, PREFERENCES_KEY_SPONSOR_BLOCK_HINT_SHOWN, false);

        // If SB is enabled or hint has been shown, exit
        if (enabled || hintShown) {
            // If SB is enabled but hint hasn't been shown, mark it as shown
            if (enabled && !hintShown) {
                SharedPrefUtils.saveBoolean(context, SponsorBlockSettings.PREFERENCES_NAME, PREFERENCES_KEY_SPONSOR_BLOCK_HINT_SHOWN, true);
            }
            return;
        }

        AlertDialog.Builder builder;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            builder = new AlertDialog.Builder(activity, android.R.style.Theme_Material_Dialog_Alert);
        } else {
            builder = new AlertDialog.Builder(activity);
        }
        builder.setTitle(str("vanced_sb"));
        builder.setIcon(VancedUtils.getIdentifier("ic_sb_logo", "drawable"));
        builder.setCancelable(false);
        builder.setMessage(str("vanced_sb_firstrun"));
        builder.setPositiveButton(str("vanced_enable"),
                (dialog, id) -> {
                    SharedPrefUtils.saveBoolean(context, SponsorBlockSettings.PREFERENCES_NAME, PREFERENCES_KEY_SPONSOR_BLOCK_HINT_SHOWN, true);
                    SharedPrefUtils.saveBoolean(context, SponsorBlockSettings.PREFERENCES_NAME, PREFERENCES_KEY_SPONSOR_BLOCK_ENABLED, true);
                    dialog.dismiss();
                });

        builder.setNegativeButton(str("vanced_disable"),
                (dialog, id) -> {
                    SharedPrefUtils.saveBoolean(context, SponsorBlockSettings.PREFERENCES_NAME, PREFERENCES_KEY_SPONSOR_BLOCK_HINT_SHOWN, true);
                    SharedPrefUtils.saveBoolean(context, SponsorBlockSettings.PREFERENCES_NAME, PREFERENCES_KEY_SPONSOR_BLOCK_ENABLED, false);
                    dialog.dismiss();
                });

        builder.setNeutralButton(str("vanced_learnmore"), null);

        AlertDialog dialog = builder.create();
        dialog.show();

        // Set black background
        dialog.getWindow().getDecorView().getBackground().setColorFilter(new LightingColorFilter(0xFF000000, VancedUtils.getIdentifier("ytBrandBackgroundSolid", "color")));

        // Set learn more action (set here so clicking it doesn't dismiss the dialog)
        dialog.getButton(DialogInterface.BUTTON_NEUTRAL).setOnClickListener(v -> {
            Uri uri = Uri.parse("https://sponsor.ajay.app/");
            Intent intent = new Intent(Intent.ACTION_VIEW, uri);
            activity.startActivity(intent);
        });
    }
}
