package app.revanced.twitch.api;

import static app.revanced.twitch.adblock.IAdblockService.channelName;
import static app.revanced.twitch.adblock.IAdblockService.isVod;

import androidx.annotation.NonNull;

import java.io.IOException;

import app.revanced.twitch.adblock.IAdblockService;
import app.revanced.twitch.adblock.PurpleAdblockService;
import app.revanced.twitch.adblock.TTVLolService;
import app.revanced.twitch.settings.SettingsEnum;
import app.revanced.twitch.utils.LogHelper;
import app.revanced.twitch.utils.ReVancedUtils;
import okhttp3.Interceptor;
import okhttp3.Response;

public class RequestInterceptor implements Interceptor {
    private IAdblockService activeService = null;

    @NonNull
    @Override
    public Response intercept(@NonNull Chain chain) throws IOException {
        var originalRequest = chain.request();

        LogHelper.debug("Intercepted request to URL: %s", originalRequest.url().toString());

        // Skip if not HLS manifest request
        if (!originalRequest.url().host().contains("usher.ttvnw.net")) {
            return chain.proceed(originalRequest);
        }

        var isVod = "no";
        if (isVod(originalRequest)) isVod = "yes";

        LogHelper.debug("Found HLS manifest request. Is VOD? %s; Channel: %s", isVod, channelName(originalRequest));

        // None of the services support VODs currently
        if (isVod(originalRequest)) return chain.proceed(originalRequest);

        updateActiveService();

        if (activeService != null) {
            var available = activeService.isAvailable();
            var rewritten = activeService.rewriteHlsRequest(originalRequest);


            if (!available || rewritten == null) {
                ReVancedUtils.toast(String.format(ReVancedUtils.getString("revanced_embedded_ads_service_unavailable"), activeService.friendlyName()), true);
                return chain.proceed(originalRequest);
            }

            LogHelper.debug("Rewritten HLS stream URL: %s", rewritten.url().toString());

            var maxAttempts = activeService.maxAttempts();

            for (var i = 1; i <= maxAttempts; i++) {
                // Execute rewritten request and close body to allow multiple proceed() calls
                var response = chain.proceed(rewritten);
                response.close();

                if (!response.isSuccessful()) {
                    LogHelper.error("Request failed (attempt %d/%d): HTTP error %d (%s)", i, maxAttempts, response.code(), response.message());
                    try {
                        Thread.sleep(50);
                    } catch (InterruptedException e) {
                        LogHelper.printException("Failed to sleep" ,e);
                    }
                } else {
                    // Accept response from ad blocker
                    LogHelper.debug("Ad-blocker used");
                    return chain.proceed(rewritten);
                }
            }

            // maxAttempts exceeded; giving up on using the ad blocker
            ReVancedUtils.toast(String.format(ReVancedUtils.getString("revanced_embedded_ads_service_failed"), activeService.friendlyName()), true);

        }

        // Adblock disabled
        return chain.proceed(originalRequest);

    }

    private void updateActiveService() {
        var current = SettingsEnum.BLOCK_EMBEDDED_ADS.getString();

        if (current.equals(ReVancedUtils.getString("key_revanced_proxy_ttv_lol")) && !(activeService instanceof TTVLolService))
            activeService = new TTVLolService();
        else if (current.equals(ReVancedUtils.getString("key_revanced_proxy_purpleadblock")) && !(activeService instanceof PurpleAdblockService))
            activeService = new PurpleAdblockService();
        else if (current.equals(ReVancedUtils.getString("key_revanced_proxy_disabled")))
            activeService = null;
    }
}
