package app.revanced.extension.pandora.ads

import app.revanced.extension.pandora.misc.hook.BaseJsonHook
import org.json.JSONObject

@Suppress("unused")
object DisableAdsHook : BaseJsonHook() {
    override fun apply(json: JSONObject) {
        json.put("hasAudioAds", false)
    }
}
