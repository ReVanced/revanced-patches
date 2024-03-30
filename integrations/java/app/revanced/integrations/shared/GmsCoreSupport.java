package app.revanced.integrations.shared;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.PowerManager;
import androidx.annotation.RequiresApi;

import java.net.MalformedURLException;
import java.net.URL;

import static app.revanced.integrations.shared.StringRef.str;

/**
 * @noinspection unused
 */
public class GmsCoreSupport {
    private static final String GMS_CORE_PACKAGE_NAME
            = getGmsCoreVendorGroupId() + ".android.gms";
    private static final Uri GMS_CORE_PROVIDER
            = Uri.parse("content://" + getGmsCoreVendorGroupId() + ".android.gsf.gservices/prefix");
    private static final String DONT_KILL_MY_APP_LINK
            = "https://dontkillmyapp.com";

    private static void open(String queryOrLink) {
        Intent intent;
        try {
            // Check if queryOrLink is a valid URL.
            new URL(queryOrLink);

            intent = new Intent(Intent.ACTION_VIEW, Uri.parse(queryOrLink));
        } catch (MalformedURLException e) {
            intent = new Intent(Intent.ACTION_WEB_SEARCH);
            intent.putExtra(SearchManager.QUERY, queryOrLink);
        }
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        Utils.getContext().startActivity(intent);

        // Gracefully exit, otherwise the broken app will continue to run.
        System.exit(0);
    }

    private static void showToastOrDialog(Context context, String toastMessageKey, String dialogMessageKey, String link) {
        if (!(context instanceof Activity)) {
            // Context is for the application and cannot show a dialog using it.
            Utils.showToastLong(str(toastMessageKey));
            open(link);
            return;
        }

        // Use a delay to allow the activity to finish initializing.
        // Otherwise, if device is in dark mode the dialog is shown with wrong color scheme.
        Utils.runOnMainThreadDelayed(() -> {
            new AlertDialog.Builder(context)
                    .setIconAttribute(android.R.attr.alertDialogIcon)
                    .setTitle(str("gms_core_dialog_title"))
                    .setMessage(str(dialogMessageKey))
                    .setPositiveButton(str("gms_core_dialog_ok_button_text"), (dialog, id) -> {
                        open(link);
                    })
                    // Manually allow using the back button to dismiss the dialog with the back button,
                    // if troubleshooting and somehow the GmsCore verification checks always fail.
                    .setCancelable(true)
                    .show();
        }, 100);
    }

    /**
     * Injection point.
     */
    @RequiresApi(api = Build.VERSION_CODES.N)
    public static void checkGmsCore(Context context) {
        try {
            // Verify GmsCore is installed.
            try {
                PackageManager manager = context.getPackageManager();
                manager.getPackageInfo(GMS_CORE_PACKAGE_NAME, PackageManager.GET_ACTIVITIES);
            } catch (PackageManager.NameNotFoundException exception) {
                Logger.printDebug(() -> "GmsCore was not found");
                // Cannot show a dialog and must show a toast,
                // because on some installations the app crashes before the dialog can display.
                Utils.showToastLong(str("gms_core_toast_not_installed_message"));
                open(getGmsCoreDownload());
                return;
            }

            // Check if GmsCore is whitelisted from battery optimizations.
            var powerManager = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
            if (!powerManager.isIgnoringBatteryOptimizations(GMS_CORE_PACKAGE_NAME)) {
                Logger.printDebug(() -> "GmsCore is not whitelisted from battery optimizations");
                showToastOrDialog(context,
                        "gms_core_toast_not_whitelisted_message",
                        "gms_core_dialog_not_whitelisted_using_battery_optimizations_message",
                        DONT_KILL_MY_APP_LINK);
                return;
            }

            // Check if GmsCore is running in the background.
            try (var client = context.getContentResolver().acquireContentProviderClient(GMS_CORE_PROVIDER)) {
                if (client == null) {
                    Logger.printDebug(() -> "GmsCore is not running in the background");
                    showToastOrDialog(context,
                            "gms_core_toast_not_whitelisted_message",
                            "gms_core_dialog_not_whitelisted_not_allowed_in_background_message",
                            DONT_KILL_MY_APP_LINK);
                }
            }
        } catch (Exception ex) {
            Logger.printException(() -> "checkGmsCore failure", ex);
        }
    }

    private static String getGmsCoreDownload() {
        final var vendorGroupId = getGmsCoreVendorGroupId();
        //noinspection SwitchStatementWithTooFewBranches
        switch (vendorGroupId) {
            case "app.revanced":
                return "https://github.com/revanced/gmscore/releases/latest";
            default:
                return vendorGroupId + ".android.gms";
        }
    }

    // Modified by a patch. Do not touch.
    private static String getGmsCoreVendorGroupId() {
        return "app.revanced";
    }
}
