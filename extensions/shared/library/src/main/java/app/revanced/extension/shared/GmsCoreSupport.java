package app.revanced.extension.shared;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.app.SearchManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.PowerManager;
import android.provider.Settings;
import android.util.Pair;
import android.widget.LinearLayout;
import androidx.annotation.Nullable;
import app.revanced.extension.shared.requests.Requester;
import app.revanced.extension.shared.requests.Route;
import app.revanced.extension.shared.settings.BaseSettings;
import app.revanced.extension.shared.ui.CustomDialog;
import org.json.JSONObject;

import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Locale;

import static app.revanced.extension.shared.StringRef.str;
import static app.revanced.extension.shared.requests.Route.Method.GET;

@SuppressWarnings("unused")
public class GmsCoreSupport {
    private static GmsCore gmsCore = GmsCore.UNKNOWN;

    static {
        for (GmsCore core : GmsCore.values()) {
            if (core.getGroupId().equals(getGmsCoreVendorGroupId())) {
                GmsCoreSupport.gmsCore = core;
                break;
            }
        }
    }

    /**
     * Injection point.
     */
    public static void checkGmsCore(Activity context) {
        gmsCore.check(context);
    }

    private static String getOriginalPackageName() {
        return null; // Modified during patching.
    }

    private static String getGmsCoreVendorGroupId() {
        return "app.revanced"; // Modified during patching.
    }


    /**
     * @return If the current package name is the same as the original unpatched app.
     * If `GmsCore support` was not included during patching, this returns true;
     */
    public static boolean isPackageNameOriginal() {
        String originalPackageName = getOriginalPackageName();
        return originalPackageName == null
                || originalPackageName.equals(Utils.getContext().getPackageName());
    }

    private enum GmsCore {
        REVANCED("app.revanced", "https://github.com/revanced/gmscore/releases/latest", () -> {
            try {
                HttpURLConnection connection = Requester.getConnectionFromRoute(
                        "https://api.github.com",
                        new Route(GET, "/repos/revanced/gmscore/releases/latest")
                );
                connection.setConnectTimeout(5000);
                connection.setReadTimeout(5000);

                int responseCode = connection.getResponseCode();
                if (responseCode != 200) {
                    Logger.printDebug(() -> "GitHub API returned status code: " + responseCode);
                    return null;
                }

                // Parse the response
                JSONObject releaseData = Requester.parseJSONObject(connection);
                String tagName = releaseData.optString("tag_name", "");
                connection.disconnect();

                if (tagName.isEmpty()) {
                    Logger.printDebug(() -> "No tag_name found in GitHub release data");
                    return null;
                }

                if (tagName.startsWith("v")) tagName = tagName.substring(1);

                return tagName;
            } catch (Exception ex) {
                Logger.printInfo(() -> "Failed to fetch latest GmsCore version from GitHub", ex);
                return null;
            }
        }),
        UNKNOWN(getGmsCoreVendorGroupId(), getGmsCoreVendorGroupId() + "android.gms", () -> null);

        private static final String DONT_KILL_MY_APP_URL
                = "https://dontkillmyapp.com/";
        private static final Route DONT_KILL_MY_APP_MANUFACTURER_API
                = new Route(GET, "/api/v2/{manufacturer}.json");
        private static final String DONT_KILL_MY_APP_NAME_PARAMETER
                = "?app=MicroG";
        private static final String BUILD_MANUFACTURER
                = Build.MANUFACTURER.toLowerCase(Locale.ROOT).replace(" ", "-");

        /**
         * If a manufacturer specific page exists on DontKillMyApp.
         */
        @Nullable
        private volatile Boolean dontKillMyAppManufacturerSupported;

        private final String groupId;
        private final String packageName;
        private final String downloadQuery;
        private final GetLatestVersion getLatestVersion;
        private final Uri gmsCoreProvider;

        GmsCore(String groupId, String downloadQuery, GetLatestVersion getLatestVersion) {
            this.groupId = groupId;
            this.packageName = groupId + ".android.gms";
            this.gmsCoreProvider = Uri.parse("content://" + groupId + ".android.gsf.gservices/prefix");

            this.downloadQuery = downloadQuery;
            this.getLatestVersion = getLatestVersion;
        }

        String getGroupId() {
            return groupId;
        }

        void check(Activity context) {
            checkInstallation(context);
            checkUpdates(context);
        }

        private void checkInstallation(Activity context) {
            try {
                // Verify the user has not included GmsCore for a root installation.
                // GmsCore Support changes the package name, but with a mounted installation
                // all manifest changes are ignored and the original package name is used.
                if (isPackageNameOriginal()) {
                    Logger.printInfo(() -> "App is mounted with root, but GmsCore patch was included");
                    // Cannot use localize text here, since the app will load resources
                    // from the unpatched app and all patch strings are missing.
                    Utils.showToastLong("The 'GmsCore support' patch breaks mount installations");

                    // Do not exit. If the app exits before launch completes (and without
                    // opening another activity), then on some devices such as Pixel phone Android 10
                    // no toast will be shown and the app will continually relaunch
                    // with the appearance of a hung app.
                }

                // Verify GmsCore is installed.
                try {
                    PackageManager manager = context.getPackageManager();
                    manager.getPackageInfo(packageName, PackageManager.GET_ACTIVITIES);
                } catch (PackageManager.NameNotFoundException exception) {
                    Logger.printInfo(() -> "GmsCore was not found");
                    // Cannot show a dialog and must show a toast,
                    // because on some installations the app crashes before a dialog can be displayed.
                    Utils.showToastLong(str("revanced_gms_core_toast_not_installed_message"));

                    open(downloadQuery);
                    return;
                }

                // Check if GmsCore is whitelisted from battery optimizations.
                if (isAndroidAutomotive(context)) {
                    // Ignore Android Automotive devices (Google built-in),
                    // as there is no way to disable battery optimizations.
                    Logger.printDebug(() -> "Device is Android Automotive");
                } else if (batteryOptimizationsEnabled(context)) {
                    Logger.printInfo(() -> "GmsCore is not whitelisted from battery optimizations");

                    showBatteryOptimizationDialog(context,
                            "revanced_gms_core_dialog_not_whitelisted_using_battery_optimizations_message",
                            "revanced_gms_core_dialog_continue_text",
                            (dialog, id) -> openGmsCoreDisableBatteryOptimizationsIntent(context));
                    return;
                }

                // Check if GmsCore is currently running in the background.
                var client = context.getContentResolver().acquireContentProviderClient(gmsCoreProvider);
                //noinspection TryFinallyCanBeTryWithResources
                try {
                    if (client == null) {
                        Logger.printInfo(() -> "GmsCore is not running in the background");
                        checkIfDontKillMyAppSupportsManufacturer();

                        showBatteryOptimizationDialog(context,
                                "revanced_gms_core_dialog_not_whitelisted_not_allowed_in_background_message",
                                "gmsrevanced_gms_core_log_open_website_text",
                                (dialog, id) -> openDontKillMyApp());
                    }
                } finally {
                    if (client != null) client.close();
                }
            } catch (Exception ex) {
                Logger.printException(() -> "checkGmsCore failure", ex);
            }
        }

        private void checkUpdates(Activity context) {
            if (!BaseSettings.GMS_CORE_CHECK_UPDATES.get()) {
                Logger.printDebug(() -> "GmsCore update check is disabled in settings");
                return;
            }

            Utils.runOnBackgroundThread(() -> {
                try {
                    PackageManager manager = context.getPackageManager();
                    String installedVersion = manager.getPackageInfo(packageName, 0).versionName;

                    Logger.printDebug(() -> "Installed GmsCore version: " + installedVersion);

                    String latestVersion = getLatestVersion.get();

                    if (latestVersion == null || latestVersion.isEmpty()) {
                        Logger.printDebug(() -> "Could not get latest GmsCore version");
                        Utils.showToastLong(str("revanced_gms_core_toast_update_check_failed_message"));
                        return;
                    }

                    Logger.printDebug(() -> "Latest GmsCore version on GitHub: " + latestVersion);

                    // Compare versions
                    if (!installedVersion.equals(latestVersion)) {
                        Logger.printInfo(() -> "GmsCore update available. Installed: " + installedVersion
                                + ", Latest: " + latestVersion);

                        showUpdateDialog(context, installedVersion, latestVersion);
                    } else {
                        Logger.printDebug(() -> "GmsCore is up to date");
                    }
                } catch (Exception ex) {
                    Logger.printInfo(() -> "Could not check GmsCore updates", ex);
                    Utils.showToastLong(str("revanced_gms_core_toast_update_check_failed_message"));
                }
            });
        }

        private void open(String queryOrLink) {
            Logger.printInfo(() -> "Opening link: " + queryOrLink);

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

        private void showUpdateDialog(Activity context, String installedVersion, String latestVersion) {
            // Use a delay to allow the activity to finish initializing.
            // Otherwise, if device is in dark mode the dialog is shown with wrong color scheme.
            Utils.runOnMainThreadDelayed(() -> {
                try {
                    Pair<Dialog, LinearLayout> dialogPair = CustomDialog.create(
                            context,
                            str("revanced_gms_core_dialog_title"),
                            String.format(str("revanced_gms_core_update_available_message"), latestVersion, installedVersion),
                            null,
                            str("revanced_gms_core_dialog_open_website_text"),
                            () -> open(downloadQuery),
                            () -> {
                            },
                            str("revanced_gms_core_dialog_cancel_text"),
                            null,
                            true
                    );

                    Dialog dialog = dialogPair.first;
                    dialog.setCancelable(true);
                    Utils.showDialog(context, dialog);
                } catch (Exception ex) {
                    Logger.printException(() -> "Failed to show GmsCore update dialog", ex);
                }
            }, 100);
        }

        private static void showBatteryOptimizationDialog(Activity context,
                                                          String dialogMessageRef,
                                                          String positiveButtonTextRef,
                                                          DialogInterface.OnClickListener onPositiveClickListener) {
            // Use a delay to allow the activity to finish initializing.
            // Otherwise, if device is in dark mode the dialog is shown with wrong color scheme.
            Utils.runOnMainThreadDelayed(() -> {
                // Create the custom dialog.
                Pair<Dialog, LinearLayout> dialogPair = CustomDialog.create(
                        context,
                        str("revanced_gms_core_dialog_title"), // Title.
                        str(dialogMessageRef), // Message.
                        null, // No EditText.
                        str(positiveButtonTextRef), // OK button text.
                        () -> onPositiveClickListener.onClick(null, 0), // Convert DialogInterface.OnClickListener to Runnable.
                        null, // No Cancel button action.
                        null, // No Neutral button text.
                        null, // No Neutral button action.
                        true // Dismiss dialog when onNeutralClick.
                );

                Dialog dialog = dialogPair.first;

                // Do not set cancelable to false to allow using back button to skip the action,
                // just in case the battery change can never be satisfied.
                dialog.setCancelable(true);

                // Show the dialog
                Utils.showDialog(context, dialog);
            }, 100);
        }

        @SuppressLint("BatteryLife") // Permission is part of GmsCore
        private void openGmsCoreDisableBatteryOptimizationsIntent(Activity activity) {
            Intent intent = new Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS);
            intent.setData(Uri.fromParts("package", packageName, null));
            activity.startActivityForResult(intent, 0);
        }

        private void checkIfDontKillMyAppSupportsManufacturer() {
            Utils.runOnBackgroundThread(() -> {
                try {
                    final long start = System.currentTimeMillis();
                    HttpURLConnection connection = Requester.getConnectionFromRoute(
                            DONT_KILL_MY_APP_URL, DONT_KILL_MY_APP_MANUFACTURER_API, BUILD_MANUFACTURER);
                    connection.setConnectTimeout(5000);
                    connection.setReadTimeout(5000);

                    final boolean supported = connection.getResponseCode() == 200;
                    Logger.printInfo(() -> "Manufacturer is " + (supported ? "" : "NOT ")
                            + "listed on DontKillMyApp: " + BUILD_MANUFACTURER
                            + " fetch took: " + (System.currentTimeMillis() - start) + "ms");
                    dontKillMyAppManufacturerSupported = supported;
                } catch (Exception ex) {
                    Logger.printInfo(() -> "Could not check if manufacturer is listed on DontKillMyApp: "
                            + BUILD_MANUFACTURER, ex);
                    dontKillMyAppManufacturerSupported = null;
                }
            });
        }

        private void openDontKillMyApp() {
            final Boolean manufacturerSupported = dontKillMyAppManufacturerSupported;

            String manufacturerPageToOpen;
            if (manufacturerSupported == null) {
                // Fetch has not completed yet. Only happens on extremely slow internet connections
                // and the user spends less than 1 second reading what's on screen.
                // Instead of waiting for the fetch (which may timeout),
                // open the website without a vendor.
                manufacturerPageToOpen = "";
            } else if (manufacturerSupported) {
                manufacturerPageToOpen = BUILD_MANUFACTURER;
            } else {
                // No manufacturer specific page exists. Open the general page.
                manufacturerPageToOpen = "general";
            }

            open(DONT_KILL_MY_APP_URL + manufacturerPageToOpen + DONT_KILL_MY_APP_NAME_PARAMETER);
        }

        /**
         * @return If GmsCore is not whitelisted from battery optimizations.
         */
        private boolean batteryOptimizationsEnabled(Context context) {
            //noinspection ObsoleteSdkInt
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
                // Android 5.0 does not have battery optimization settings.
                return false;
            }
            var powerManager = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
            return !powerManager.isIgnoringBatteryOptimizations(packageName);
        }

        private boolean isAndroidAutomotive(Context context) {
            return context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_AUTOMOTIVE);
        }
    }

    @FunctionalInterface
    private interface GetLatestVersion {
        String get();
    }
}
