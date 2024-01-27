package app.revanced.integrations.twitch.api;

import androidx.annotation.NonNull;
import app.revanced.integrations.shared.Logger;
import app.revanced.integrations.shared.Utils;
import app.revanced.integrations.twitch.adblock.IAdblockService;
import app.revanced.integrations.twitch.adblock.LuminousService;
import app.revanced.integrations.twitch.adblock.PurpleAdblockService;
import app.revanced.integrations.twitch.settings.Settings;
import okhttp3.Interceptor;
import okhttp3.Response;

import java.io.IOException;

import static app.revanced.integrations.shared.StringRef.str;

public class RequestInterceptor implements Interceptor {
    private IAdblockService activeService = null;

    private static final String PROXY_DISABLED = str("key_revanced_proxy_disabled");
    private static final String LUMINOUS_SERVICE = str("key_revanced_proxy_luminous");
    private static final String PURPLE_ADBLOCK_SERVICE = str("key_revanced_proxy_purpleadblock");


    @NonNull
    @Override
    public Response intercept(@NonNull Chain chain) throws IOException {
        var originalRequest = chain.request();

        if (Settings.BLOCK_EMBEDDED_ADS.get().equals(PROXY_DISABLED)) {
            return chain.proceed(originalRequest);
        }

        Logger.printDebug(() -> "Intercepted request to URL:" + originalRequest.url());

        // Skip if not HLS manifest request
        if (!originalRequest.url().host().contains("usher.ttvnw.net")) {
            return chain.proceed(originalRequest);
        }

        final String isVod;
        if (IAdblockService.isVod(originalRequest)) isVod = "yes";
        else isVod = "no";

        Logger.printDebug(() -> "Found HLS manifest request. Is VOD? " +
                isVod +
                "; Channel: " +
                IAdblockService.channelName(originalRequest)
        );

        // None of the services support VODs currently
        if (IAdblockService.isVod(originalRequest)) return chain.proceed(originalRequest);

        updateActiveService();

        if (activeService != null) {
            var available = activeService.isAvailable();
            var rewritten = activeService.rewriteHlsRequest(originalRequest);


            if (!available || rewritten == null) {
                Utils.showToastShort(String.format(
                        str("revanced_embedded_ads_service_unavailable"), activeService.friendlyName()
                ));
                return chain.proceed(originalRequest);
            }

            Logger.printDebug(() -> "Rewritten HLS stream URL: " + rewritten.url());

            var maxAttempts = activeService.maxAttempts();

            for (var i = 1; i <= maxAttempts; i++) {
                // Execute rewritten request and close body to allow multiple proceed() calls
                var response = chain.proceed(rewritten);
                response.close();

                if (!response.isSuccessful()) {
                    int attempt = i;
                    Logger.printException(() -> "Request failed (attempt " +
                            attempt +
                            "/" + maxAttempts + "): HTTP error " +
                            response.code() +
                            " (" + response.message() + ")"
                    );

                    try {
                        Thread.sleep(50);
                    } catch (InterruptedException e) {
                        Logger.printException(() -> "Failed to sleep", e);
                    }
                } else {
                    // Accept response from ad blocker
                    Logger.printDebug(() -> "Ad-blocker used");
                    return chain.proceed(rewritten);
                }
            }

            // maxAttempts exceeded; giving up on using the ad blocker
            Utils.showToastLong(String.format(
                    str("revanced_embedded_ads_service_failed"),
                    activeService.friendlyName())
            );
        }

        // Adblock disabled
        return chain.proceed(originalRequest);

    }

    private void updateActiveService() {
        var current = Settings.BLOCK_EMBEDDED_ADS.get();

        if (current.equals(LUMINOUS_SERVICE) && !(activeService instanceof LuminousService))
            activeService = new LuminousService();
        else if (current.equals(PURPLE_ADBLOCK_SERVICE) && !(activeService instanceof PurpleAdblockService))
            activeService = new PurpleAdblockService();
        else if (current.equals(PROXY_DISABLED))
            activeService = null;
    }
}
