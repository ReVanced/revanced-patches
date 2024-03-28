package app.revanced.integrations.shared;

import android.app.SearchManager;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import androidx.annotation.RequiresApi;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Objects;

import static app.revanced.integrations.shared.StringRef.str;

/**
 * @noinspection unused
 */
public class GmsCoreSupport {
    private static final String GMS_CORE_PACKAGE_NAME
            = getGmsCoreVendor() + ".android.gms";
    private static final String DONT_KILL_MY_APP_LINK
            = "https://dontkillmyapp.com";
    private static final Uri GMS_CORE_PROVIDER
            = Uri.parse("content://" + getGmsCoreVendor() + ".android.gsf.gservices/prefix");

    private static void open(String queryOrLink, String message) {
        Utils.showToastLong(message);

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

        // Gracefully exit, otherwise without Gms the app crashes and Android can nag the user.
        System.exit(0);
    }

    /**
     * Injection point.
     */
    @RequiresApi(api = Build.VERSION_CODES.N)
    public static void checkAvailability() {
        var context = Objects.requireNonNull(Utils.getContext());

        try {
            context.getPackageManager().getPackageInfo(GMS_CORE_PACKAGE_NAME, PackageManager.GET_ACTIVITIES);
        } catch (PackageManager.NameNotFoundException exception) {
            Logger.printInfo(() -> "GmsCore was not found", exception);
            open(getGmsCoreDownload(), str("gms_core_not_installed_warning"));
            return;
        }

        try (var client = context.getContentResolver().acquireContentProviderClient(GMS_CORE_PROVIDER)) {
            if (client != null) return;

            Logger.printInfo(() -> "GmsCore is not running in the background");
            open(DONT_KILL_MY_APP_LINK, str("gms_core_not_running_warning"));
        } catch (Exception ex) {
            Logger.printException(() -> "Could not check GmsCore background task", ex);
        }
    }

    private static String getGmsCoreDownload() {
        final var vendor = getGmsCoreVendor();
        //noinspection SwitchStatementWithTooFewBranches
        switch (vendor) {
            case "app.revanced":
                return "https://github.com/revanced/gmscore/releases/latest";
            default:
                return vendor + ".android.gms";
        }
    }

    // Modified by a patch. Do not touch.
    private static String getGmsCoreVendor() {
        return "app.revanced";
    }
}
