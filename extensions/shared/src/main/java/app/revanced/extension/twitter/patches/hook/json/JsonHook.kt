package app.revanced.extension.twitter.patches.hook.json

import app.revanced.extension.twitter.patches.hook.patch.Hook
import org.json.JSONObject

interface JsonHook : Hook<JSONObject> {
    /**
     * Transform a JSONObject.
     *
     * @param json The JSONObject.
     */
    fun transform(json: JSONObject): JSONObject

    override fun hook(type: JSONObject) = transform(type)
}
