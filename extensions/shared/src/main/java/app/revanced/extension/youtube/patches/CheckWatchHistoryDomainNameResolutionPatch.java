package app.revanced.extension.youtube.patches;

import static app.revanced.extension.shared.StringRef.str;

import android.app.Activity;
import android.text.Html;

import java.net.InetAddress;
import java.net.UnknownHostException;

import app.revanced.extension.shared.Logger;
import app.revanced.extension.shared.Utils;
import app.revanced.extension.youtube.settings.Settings;

@SuppressWarnings("unused")
public class CheckWatchHistoryDomainNameResolutionPatch {

    private static final String HISTORY_TRACKING_ENDPOINT = "s.youtube.com";

    private static final String SINKHOLE_IPV4 = "0.0.0.0";
    private static final String SINKHOLE_IPV6 = "::";

    /** @noinspection SameParameterValue */
    private static boolean domainResolvesToValidIP(String host) {
        try {
            InetAddress address = InetAddress.getByName(host);
            String hostAddress = address.getHostAddress();

            if (address.isLoopbackAddress()) {
                Logger.printDebug(() -> host + " resolves to localhost");
            } else if (SINKHOLE_IPV4.equals(hostAddress) || SINKHOLE_IPV6.equals(hostAddress)) {
                Logger.printDebug(() -> host + " resolves to sinkhole ip");
            } else {
                return true; // Domain is not blocked.
            }
        } catch (UnknownHostException e) {
            Logger.printDebug(() -> host + " failed to resolve");
        }

        return false;
    }

    /**
     * Injection point.
     *
     * Checks if s.youtube.com is blacklisted and playback history will fail to work.
     */
    public static void checkDnsResolver(Activity context) {
        if (!Utils.isNetworkConnected() || !Settings.CHECK_WATCH_HISTORY_DOMAIN_NAME.get()) return;

        Utils.runOnBackgroundThread(() -> {
            try {
                if (domainResolvesToValidIP(HISTORY_TRACKING_ENDPOINT)) {
                    return;
                }

                Utils.runOnMainThread(() -> {
                    var alertDialog = new android.app.AlertDialog.Builder(context)
                            .setTitle(str("revanced_check_watch_history_domain_name_dialog_title"))
                            .setMessage(Html.fromHtml(str("revanced_check_watch_history_domain_name_dialog_message")))
                            .setIconAttribute(android.R.attr.alertDialogIcon)
                            .setPositiveButton(android.R.string.ok, (dialog, which) -> {
                                dialog.dismiss();
                            }).setNegativeButton(str("revanced_check_watch_history_domain_name_dialog_ignore"), (dialog, which) -> {
                                Settings.CHECK_WATCH_HISTORY_DOMAIN_NAME.save(false);
                                dialog.dismiss();
                            })
                            .setCancelable(false)
                            .show();
                });
            } catch (Exception ex) {
                Logger.printException(() -> "checkDnsResolver failure", ex);
            }
        });
    }
}
