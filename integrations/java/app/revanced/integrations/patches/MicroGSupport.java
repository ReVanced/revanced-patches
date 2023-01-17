package app.revanced.integrations.patches;

import static app.revanced.integrations.sponsorblock.StringRef.str;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.widget.Toast;

import app.revanced.integrations.utils.LogHelper;
import app.revanced.integrations.utils.ReVancedUtils;

public class MicroGSupport {
    private static final String MICROG_VENDOR = "com.mgoogle";
    private static final String MICROG_PACKAGE_NAME = "com.mgoogle.android.gms";
    private static final String VANCED_MICROG_DOWNLOAD_LINK = "https://github.com/TeamVanced/VancedMicroG/releases/latest";

    public static void checkAvailability() {
        var context = ReVancedUtils.getContext();
        assert context != null;
        try {
            context.getPackageManager().getPackageInfo(MICROG_PACKAGE_NAME, PackageManager.GET_ACTIVITIES);
            LogHelper.printDebug(() -> "MicroG is installed on the device");
        } catch (PackageManager.NameNotFoundException exception) {
            LogHelper.printException(() -> ("MicroG was not found"), exception);
            Toast.makeText(context, str("microg_not_installed_warning"), Toast.LENGTH_LONG).show();

            var intent = new Intent(Intent.ACTION_VIEW);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.setData(Uri.parse(VANCED_MICROG_DOWNLOAD_LINK));
            context.startActivity(intent);
        }
    }
}
