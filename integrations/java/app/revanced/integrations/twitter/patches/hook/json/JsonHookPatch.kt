package app.revanced.integrations.twitter.patches.hook.json

import app.revanced.integrations.twitter.patches.hook.patch.dummy.DummyHook
import app.revanced.integrations.twitter.utils.json.JsonUtils.parseJson
import app.revanced.integrations.twitter.utils.stream.StreamUtils
import org.json.JSONException
import java.io.IOException
import java.io.InputStream

object JsonHookPatch {
    // Additional hooks added by corresponding patch.
    private val hooks = buildList<JsonHook> {
        add(DummyHook)
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