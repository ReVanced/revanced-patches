package app.revanced.patches.reddit.customclients

import android.os.Environment
import app.revanced.extensions.exception
import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.fingerprint.method.impl.MethodFingerprint
import app.revanced.patcher.fingerprint.method.impl.MethodFingerprintResult
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.OptionsContainer
import app.revanced.patcher.patch.PatchException
import app.revanced.patcher.patch.PatchOption
import java.io.File

abstract class AbstractSpoofClientPatch(
    private val redirectUri: String,
    private val options: SpoofClientOptionsContainer,
    private val clientIdFingerprints: List<MethodFingerprint>,
    private val userAgentFingerprints: List<MethodFingerprint>? = null,
) : BytecodePatch(buildList {
    addAll(clientIdFingerprints)
    userAgentFingerprints?.let(::addAll)
}) {
    override fun execute(context: BytecodeContext) {
        if (options.clientId == null) {
            // Ensure device runs Android.
            try {
                Class.forName("android.os.Environment")
            } catch (e: ClassNotFoundException) {
                throw PatchException("No client ID provided")
            }

            File(Environment.getExternalStorageDirectory(), "reddit_client_id_revanced.txt").also {
                if (it.exists()) return@also

                val error = """
                    In order to use this patch, you need to provide a client ID.
                    You can do that by creating a file at ${it.absolutePath} with the client ID as its content.
                    Alternatively, you can provide the client ID using patch options.
                    
                    You can get your client ID from https://www.reddit.com/prefs/apps.
                    The application type has to be "Installed app" and the redirect URI has to be set to "$redirectUri".
                """.trimIndent()

                throw PatchException(error)
            }.let { options.clientId = it.readText().trim() }
        }

        fun List<MethodFingerprint>?.executePatch(
            patch: List<MethodFingerprintResult>.(BytecodeContext) -> Unit
        ) = this?.map { it.result ?: throw it.exception }?.patch(context)

        clientIdFingerprints.executePatch { patchClientId(context) }
        userAgentFingerprints.executePatch { patchUserAgent(context) }
    }

    /**
     * Patch the client ID. The fingerprints are guaranteed to be in the same order as in [clientIdFingerprints].
     *
     * @param context The current [BytecodeContext].
     *
     */
    abstract fun List<MethodFingerprintResult>.patchClientId(context: BytecodeContext)

    /**
     * Patch the user agent. The fingerprints are guaranteed to be in the same order as in [userAgentFingerprints].
     *
     * @param context The current [BytecodeContext].
     */
    // Not every client needs to patch the user agent.
    open fun List<MethodFingerprintResult>.patchUserAgent(context: BytecodeContext) {}

    companion object Options {
        open class SpoofClientOptionsContainer : OptionsContainer() {
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