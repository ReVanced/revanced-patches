package app.revanced.twitch.api

import app.revanced.twitch.adblock.IAdblockService
import app.revanced.twitch.adblock.IAdblockService.Companion.channelName
import app.revanced.twitch.adblock.IAdblockService.Companion.isVod
import app.revanced.twitch.adblock.PurpleAdblockService
import app.revanced.twitch.adblock.TTVLolService
import app.revanced.twitch.settings.SettingsEnum
import app.revanced.twitch.utils.LogHelper
import app.revanced.twitch.utils.ReVancedUtils
import okhttp3.*

class RequestInterceptor : Interceptor {
    private var activeService: IAdblockService? = null

    private fun updateActiveService() {
        val current = SettingsEnum.BLOCK_EMBEDDED_ADS.string
        activeService = if(current == ReVancedUtils.getString("key_revanced_proxy_ttv_lol") && activeService !is TTVLolService)
            TTVLolService()
        else if(current == ReVancedUtils.getString("key_revanced_proxy_purpleadblock") && activeService !is PurpleAdblockService)
            PurpleAdblockService()
        else if(current == ReVancedUtils.getString("key_revanced_proxy_disabled"))
            null
        else
            activeService
    }

    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        LogHelper.debug("Intercepted request to URL: %s", originalRequest.url.toString())

        // Skip if not HLS manifest request
        if (!originalRequest.url.host.contains("usher.ttvnw.net")) {
            return chain.proceed(originalRequest)
        }

        LogHelper.debug("Found HLS manifest request. Is VOD? %s; Channel: %s",
            if (originalRequest.isVod()) "yes" else "no", originalRequest.channelName())

        // None of the services support VODs currently
        if(originalRequest.isVod())
            return chain.proceed(originalRequest)

        updateActiveService()

        activeService?.let {
            val available = it.isAvailable()
            val rewritten = it.rewriteHlsRequest(originalRequest)

            if (!available || rewritten == null) {
                ReVancedUtils.toast(
                    String.format(ReVancedUtils.getString("revanced_embedded_ads_service_unavailable"), it.friendlyName()),
                    true
                )
                return chain.proceed(originalRequest)
            }

            LogHelper.debug("Rewritten HLS stream URL: %s", rewritten.url.toString())

            val maxAttempts = it.maxAttempts()
            for(i in 1..maxAttempts) {
                // Execute rewritten request and close body to allow multiple proceed() calls
                val response = chain.proceed(rewritten).apply { close() }
                if(!response.isSuccessful) {
                    LogHelper.error("Request failed (attempt %d/%d): HTTP error %d (%s)",
                        i, maxAttempts, response.code, response.message)
                    Thread.sleep(50)
                }
                else {
                    // Accept response from ad blocker
                    LogHelper.debug("Ad-blocker used")
                    return chain.proceed(rewritten)
                }
            }

            // maxAttempts exceeded; giving up on using the ad blocker
            ReVancedUtils.toast(
                String.format(ReVancedUtils.getString("revanced_embedded_ads_service_failed"), it.friendlyName()),
                true
            )
        }

        // Adblock disabled
        return chain.proceed(originalRequest)
    }
}