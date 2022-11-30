package app.revanced.twitch.adblock

import app.revanced.twitch.adblock.IAdblockService.Companion.channelName
import app.revanced.twitch.api.RetrofitClient
import app.revanced.twitch.utils.LogHelper
import app.revanced.twitch.utils.ReVancedUtils
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import okhttp3.Request
import okhttp3.ResponseBody

class PurpleAdblockService : IAdblockService {
    private val tunnels = mutableMapOf(
        /* tunnel url */        /* alive */
        "https://eu1.jupter.ga" to false,
        "https://eu2.jupter.ga" to false
    )

    override fun friendlyName(): String = ReVancedUtils.getString("revanced_proxy_purpleadblock")

    override fun maxAttempts(): Int = 3

    override fun isAvailable(): Boolean {
        for(tunnel in tunnels.keys) {
            var success = true
            try {
                val response = RetrofitClient.getInstance().purpleAdblockApi.ping(tunnel).execute()
                if (!response.isSuccessful) {
                    LogHelper.error("PurpleAdBlock tunnel $tunnel returned an error: HTTP code %d", response.code())
                    LogHelper.debug(response.message())
                    LogHelper.debug((response.errorBody() as ResponseBody).string())
                    success = false
                }
            } catch (ex: Exception) {
                LogHelper.printException("PurpleAdBlock tunnel $tunnel is unavailable", ex)
                success = false
            }

            // Cache availability data
            tunnels[tunnel] = success

            if(success)
                return true
        }

        return false
    }

    override fun rewriteHlsRequest(originalRequest: Request): Request? {
        val server = tunnels.filter { it.value }.map { it.key }.firstOrNull()
        server ?: run {
            LogHelper.error("No tunnels are available")
            return null
        }

        // Compose new URL
        val url = "$server/channel/${originalRequest.channelName()}".toHttpUrlOrNull()
        if (url == null) {
            LogHelper.error("Failed to parse rewritten URL")
            return null
        }

        // Overwrite old request
        return Request.Builder()
            .get()
            .url(url)
            .build()
    }
}