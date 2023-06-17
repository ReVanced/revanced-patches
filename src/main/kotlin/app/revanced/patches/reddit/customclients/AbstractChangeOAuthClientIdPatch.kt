package app.revanced.patches.reddit.customclients

import android.os.Environment
import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.fingerprint.method.impl.MethodFingerprint
import app.revanced.patcher.patch.*
import app.revanced.patches.reddit.customclients.boostforreddit.api.patch.ChangeOAuthClientIdPatch
import app.revanced.patches.reddit.customclients.syncforreddit.api.patch.ChangeOAuthClientIdPatch.Companion.clientId
import java.io.File

abstract class AbstractChangeOAuthClientIdPatch(
    private val redirectUri: String,
    private val fingerprint: MethodFingerprint,
) : BytecodePatch(listOf(fingerprint)) {
    override fun execute(context: BytecodeContext): PatchResult {
        if (ChangeOAuthClientIdPatch.clientId == null) {
            // Test if on Android
            try {
                Class.forName("android.os.Environment")
            } catch (e: ClassNotFoundException) {
                return PatchResultError("No client ID provided")
            }

            File(Environment.getExternalStorageDirectory(), "reddit_client_id_revanced.txt").also {
                if (it.exists()) return@also

                val error = """
                    In order to use this patch, you need to provide a client ID.
                    You can do this by creating a file at ${it.absolutePath} with the client ID as its content.
                    Alternatively, you can provide the client ID using patch options.
                    
                    You can get your client ID from https://www.reddit.com/prefs/apps.
                    The application type has to be "installed app" and the redirect URI has to be set to "$redirectUri"
                """.trimIndent()

                return PatchResultError(error)
            }.let { clientId = it.readText().trim() }
        }

        return fingerprint.patch(context)
    }

    abstract fun MethodFingerprint.patch(context: BytecodeContext): PatchResult

    companion object Options {
        open class ChangeOAuthClientIdOptionsContainer : OptionsContainer() {
            var clientId by option(
                PatchOption.StringOption(
                    "client-id",
                    null,
                    "OAuth client ID",
                    "The Reddit OAuth client ID."
                )
            )
        }
    }
}