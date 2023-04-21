package app.revanced.integrations.patches;

import static app.revanced.integrations.utils.StringRef.str;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;

import java.util.Objects;

import app.revanced.integrations.utils.LogHelper;
import app.revanced.integrations.utils.ReVancedUtils;

public class MicroGSupport {
    private static final String MICROG_VENDOR = "com.mgoogle";
    private static final String MICROG_PACKAGE_NAME = MICROG_VENDOR + ".android.gms";
    private static final String VANCED_MICROG_DOWNLOAD_LINK = "https://github.com/TeamVanced/VancedMicroG/releases/latest";
    private static final String DONT_KILL_MY_APP_LINK = "https://dontkillmyapp.com";
    private static final Uri VANCED_MICROG_PROVIDER = Uri.parse("content://" + MICROG_VENDOR + ".android.gsf.gservices/prefix");

    private static void startIntent(Context context, String uriString, String message) {
        ReVancedUtils.showToastLong(message);

        var intent = new Intent(Intent.ACTION_VIEW);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setData(Uri.parse(uriString));
        context.startActivity(intent);
    }

    public static void checkAvailability() {
        var context = Objects.requireNonNull(ReVancedUtils.getContext());

        try {
            context.getPackageManager().getPackageInfo(MICROG_PACKAGE_NAME, PackageManager.GET_ACTIVITIES);
        } catch (PackageManager.NameNotFoundException exception) {
            LogHelper.printInfo(() -> "Vanced MicroG was not found", exception);
            startIntent(context, VANCED_MICROG_DOWNLOAD_LINK, str("microg_not_installed_warning"));

            // Gracefully exit the app, so it does not crash.
            System.exit(0);
        }

        try (var client = context.getContentResolver().acquireContentProviderClient(VANCED_MICROG_PROVIDER)) {
            if (client != null) return;
            LogHelper.printInfo(() -> "Vanced MicroG is not running in the background");
            startIntent(context, DONT_KILL_MY_APP_LINK, str("microg_not_running_warning"));
        }
    }
}
