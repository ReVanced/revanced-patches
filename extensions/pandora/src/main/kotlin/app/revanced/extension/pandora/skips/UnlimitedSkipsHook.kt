package app.revanced.extension.pandora.skips

import app.revanced.extension.pandora.misc.hook.BaseJsonHook
import org.json.JSONObject

@Suppress("unused")
object UnlimitedSkipsHook : BaseJsonHook() {
    override fun apply(json: JSONObject) {
        json.put("skipLimitBehavior", "unlimited")
    }
}
