package app.revanced.integrations.twitter.patches.hook.twifucker

import org.json.JSONArray
import org.json.JSONObject

internal object TwiFuckerUtils {
    inline fun JSONArray.forEach(action: (JSONObject) -> Unit) {
        (0 until this.length()).forEach { i ->
            if (this[i] is JSONObject) {
                action(this[i] as JSONObject)
            }
        }
    }

    inline fun JSONArray.forEachIndexed(action: (index: Int, JSONObject) -> Unit) {
        (0 until this.length()).forEach { i ->
            if (this[i] is JSONObject) {
                action(i, this[i] as JSONObject)
            }
        }
    }
}
