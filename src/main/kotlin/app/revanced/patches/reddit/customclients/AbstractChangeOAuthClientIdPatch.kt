package app.revanced.patches.reddit.customclients

import android.os.Environment
import app.revanced.extensions.toErrorResult
import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.fingerprint.method.impl.MethodFingerprint
import app.revanced.patcher.fingerprint.method.impl.MethodFingerprintResult
import app.revanced.patcher.patch.*
import java.io.File

abstract class AbstractChangeOAuthClientIdPatch(
    private val redirectUri: String,
    private val options: ChangeOAuthClientIdOptionsContainer,
    private val fingerprints: List<MethodFingerprint>
) : BytecodePatch(fingerprints) {
    override fun execute(context: BytecodeContext): PatchResult {
        if (options.clientId == null) {
            // Ensure device runs Android.
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
                    The application type has to be "Installed app" and the redirect URI has to be set to "$redirectUri".
                """.trimIndent()

                return PatchResultError(error)
            }.let { options.clientId = it.readText().trim() }
        }

        return fingerprints.map { it.result ?: throw it.toErrorResult() }.patch(context)
    }

    abstract fun List<MethodFingerprintResult>.patch(context: BytecodeContext): PatchResult

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