package app.revanced.extension.twitch.api;

import androidx.annotation.NonNull;
import app.revanced.extension.shared.Logger;
import app.revanced.extension.shared.Utils;
import app.revanced.extension.twitch.adblock.IAdblockService;
import app.revanced.extension.twitch.adblock.LuminousService;
import app.revanced.extension.twitch.adblock.PurpleAdblockService;
import app.revanced.extension.twitch.settings.Settings;
import okhttp3.Interceptor;
import okhttp3.Response;

import java.io.IOException;

import static app.revanced.extension.shared.StringRef.str;

public class RequestInterceptor implements Interceptor {
    private IAdblockService activeService = null;

    private static final String PROXY_DISABLED = str("revanced_block_embedded_ads_entry_1");
    private static final String LUMINOUS_SERVICE = str("revanced_block_embedded_ads_entry_2");
    private static final String PURPLE_ADBLOCK_SERVICE = str("revanced_block_embedded_ads_entry_3");


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
