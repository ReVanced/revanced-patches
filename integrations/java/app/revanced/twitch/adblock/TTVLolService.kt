package app.revanced.twitch.adblock

import app.revanced.twitch.adblock.IAdblockService.Companion.channelName
import app.revanced.twitch.adblock.IAdblockService.Companion.isVod
import app.revanced.twitch.utils.LogHelper
import app.revanced.twitch.utils.ReVancedUtils
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import okhttp3.Request
import java.util.Random

class TTVLolService : IAdblockService {

    override fun friendlyName(): String = ReVancedUtils.getString("revanced_proxy_ttv_lol")

    // TTV.lol is sometimes unstable
    override fun maxAttempts(): Int = 4

    override fun isAvailable(): Boolean = true

    override fun rewriteHlsRequest(originalRequest: Request): Request? {
        // Compose new URL
        val url = "https://api.ttv.lol/${if (originalRequest.isVod()) "vod" else "playlist"}/${originalRequest.channelName()}.m3u8${nextQuery()}".toHttpUrlOrNull()
        if (url == null) {
            LogHelper.error("Failed to parse rewritten URL")
            return null
        }

        // Overwrite old request
        return Request.Builder()
            .get()
            .url(url)
            .addHeader("X-Donate-To", "https://ttv.lol/donate")
            .build()
    }

    private fun nextQuery(): String {
        return SAMPLE_QUERY.replace("<SESSION>", generateSessionId())
    }

    private fun generateSessionId() =
        (1..32)
            .map { "abcdef0123456789"[randomSource.nextInt(16)] }
            .joinToString("")

    private val randomSource = Random()

    companion object {

        private const val SAMPLE_QUERY =
            "%3Fallow_source%3Dtrue%26fast_bread%3Dtrue%26allow_audio_only%3Dtrue%26p%3D0%26play_session_id%3D<SESSION>%26player_backend%3Dmediaplayer%26warp%3Dfalse%26force_preroll%3Dfalse%26mobile_cellular%3Dfalse"
    }
}