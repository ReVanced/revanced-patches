package app.revanced.integrations.twitter.patches.hook.json

import org.json.JSONObject

abstract class BaseJsonHook : JsonHook {
    abstract fun apply(json: JSONObject)

    override fun transform(json: JSONObject) = json.apply { apply(json) }
}