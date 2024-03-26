package app.revanced.integrations.youtube.patches;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import androidx.annotation.RequiresApi;
import app.revanced.integrations.shared.Logger;
import app.revanced.integrations.shared.Utils;

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

    private static void search(Context context, String uriString, String message) {
        Utils.showToastLong(message);

        var intent = new Intent(Intent.ACTION_WEB_SEARCH);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra(SearchManager.QUERY, uriString);
        context.startActivity(intent);
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    public static void checkAvailability() {
        var context = Objects.requireNonNull(Utils.getContext());

        try {
            context.getPackageManager().getPackageInfo(GMS_CORE_PACKAGE_NAME, PackageManager.GET_ACTIVITIES);
        } catch (PackageManager.NameNotFoundException exception) {
            Logger.printInfo(() -> "GmsCore was not found", exception);
            search(context, getGmsCoreDownloadLink(), str("gms_core_not_installed_warning"));

            System.exit(0);
        }

        try (var client = context.getContentResolver().acquireContentProviderClient(GMS_CORE_PROVIDER)) {
            if (client != null) return;

            Logger.printInfo(() -> "GmsCore is not running in the background");
            search(context, DONT_KILL_MY_APP_LINK, str("gms_core_not_running_warning"));

            System.exit(0);
        }
    }

    private static String getGmsCoreDownloadLink() {
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
