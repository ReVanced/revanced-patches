package app.revanced.twitch.adblock

import okhttp3.Request

interface IAdblockService {
    fun friendlyName(): String
    fun maxAttempts(): Int
    fun isAvailable(): Boolean
    fun rewriteHlsRequest(originalRequest: Request): Request?

    companion object {
        fun Request.isVod() = url.pathSegments.contains("vod")
        fun Request.channelName() =
            url.pathSegments
                .firstOrNull { it.endsWith(".m3u8") }
                .run { this?.replace(".m3u8", "") }
    }
}