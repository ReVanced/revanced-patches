package app.revanced.extension.pandora.misc.hook

import org.json.JSONObject

/**
 * Dummy hook to reserve a register in [JsonHookPatch.hooks] list.
 */
object DummyHook : BaseJsonHook() {
    override fun apply(json: JSONObject) {
        // Do nothing.
    }
}
