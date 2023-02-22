package app.revanced.twitter.patches.hook.json

import app.revanced.twitter.utils.json.JsonUtils.parseJson
import app.revanced.twitter.utils.stream.StreamUtils
import org.json.JSONException
import java.io.IOException
import java.io.InputStream

object JsonHookPatch {
    private val hooks = buildList<JsonHook> {
        // Modified by corresponding patch.
    }

    @JvmStatic
    fun parseJsonHook(jsonInputStream: InputStream): InputStream {
        var jsonObject = try {
            parseJson(jsonInputStream)
        } catch (ignored: IOException) {
            return jsonInputStream // Unreachable.
        } catch (ignored: JSONException) {
            return jsonInputStream
        }

        for (hook in hooks) jsonObject = hook.hook(jsonObject)

        return StreamUtils.fromString(jsonObject.toString())
    }
}