package app.revanced.extension.pandora.misc.hook

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
