package app.revanced.extension.pandora.misc.hook

import org.json.JSONObject

object JsonHookPatch {
    // Additional hooks added by corresponding patch.
    private val hooks = buildList<JsonHook> {
        add(DummyHook)
    }

    @JvmStatic
    fun overrideJsonHook(jsonObject: JSONObject) {
        for (hook in hooks)
            hook.hook(jsonObject)
    }
}
