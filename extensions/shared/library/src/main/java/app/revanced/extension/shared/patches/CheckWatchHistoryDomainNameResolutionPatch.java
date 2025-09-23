package app.revanced.extension.shared.patches;

import static app.revanced.extension.shared.StringRef.str;

import android.app.Activity;
import android.app.Dialog;
import android.text.Html;
import android.util.Pair;
import android.widget.LinearLayout;

import java.net.InetAddress;
import java.net.UnknownHostException;

import app.revanced.extension.shared.Logger;
import app.revanced.extension.shared.Utils;
import app.revanced.extension.shared.settings.BaseSettings;
import app.revanced.extension.shared.ui.CustomDialog;

@SuppressWarnings("unused")
public class CheckWatchHistoryDomainNameResolutionPatch {

    private static final String HISTORY_TRACKING_ENDPOINT = "s.youtube.com";

    private static final String SINKHOLE_IPV4 = "0.0.0.0";
    private static final String SINKHOLE_IPV6 = "::";

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
        if (!Utils.isNetworkConnected() || !BaseSettings.CHECK_WATCH_HISTORY_DOMAIN_NAME.get()) return;

        Utils.runOnBackgroundThread(() -> {
            try {
                // If the user has a flaky DNS server, or they just lost internet connectivity
                // and the isNetworkConnected() check has not detected it yet (it can take a few
                // seconds after losing connection), then the history tracking endpoint will
                // show a resolving error but it's actually an internet connection problem.
                //
                // Prevent this false positive by verify youtube.com resolves.
                // If youtube.com does not resolve, then it's not a watch history domain resolving error
                // because the entire app will not work since no domains are resolving.
                if (!domainResolvesToValidIP("youtube.com")
                        || domainResolvesToValidIP(HISTORY_TRACKING_ENDPOINT)) {
                    return;
                }

                Utils.runOnMainThread(() -> {
                    try {
                        // Create the custom dialog.
                        Pair<Dialog, LinearLayout> dialogPair = CustomDialog.create(
                                context,
                                str("revanced_check_watch_history_domain_name_dialog_title"), // Title.
                                Html.fromHtml(str("revanced_check_watch_history_domain_name_dialog_message")), // Message (HTML).
                                null, // No EditText.
                                null, // OK button text.
                                () -> {}, // OK button action (just dismiss).
                                () -> {}, // Cancel button action (just dismiss).
                                str("revanced_check_watch_history_domain_name_dialog_ignore"), // Neutral button text.
                                () -> BaseSettings.CHECK_WATCH_HISTORY_DOMAIN_NAME.save(false),    // Neutral button action (Ignore).
                                true // Dismiss dialog on Neutral button click.
                        );

                        // Show the dialog.
                        Dialog dialog = dialogPair.first;

                        Utils.showDialog(context, dialog, false, null);
                    } catch (Exception ex) {
                        Logger.printException(() -> "checkDnsResolver dialog creation failure", ex);
                    }
                });
            } catch (Exception ex) {
                Logger.printException(() -> "checkDnsResolver failure", ex);
            }
        });
    }
}
