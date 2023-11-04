package app.revanced.patches.reddit.customclients

import app.revanced.extensions.exception
import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.fingerprint.MethodFingerprint
import app.revanced.patcher.fingerprint.MethodFingerprintResult
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.options.PatchOption.PatchExtensions.stringPatchOption

abstract class AbstractSpoofClientPatch(
    redirectUri: String,
    private val clientIdFingerprints: List<MethodFingerprint>,
    private val userAgentFingerprints: List<MethodFingerprint>? = null,
    private val miscellaneousFingerprints: List<MethodFingerprint>? = null
) : BytecodePatch(buildSet {
    addAll(clientIdFingerprints)
    userAgentFingerprints?.let(::addAll)
    miscellaneousFingerprints?.let(::addAll)
}) {
    var clientId by stringPatchOption(
        "client-id",
        null,
        null,
        "OAuth client ID",
        "The Reddit OAuth client ID. " +
                "You can get your client ID from https://www.reddit.com/prefs/apps. " +
                "The application type has to be \"Installed app\" " +
                "and the redirect URI has to be set to \"$redirectUri\".",
        true
    )

    override fun execute(context: BytecodeContext) {
        fun List<MethodFingerprint>?.executePatch(
            patch: List<MethodFingerprintResult>.(BytecodeContext) -> Unit
        ) = this?.map { it.result ?: throw it.exception }?.patch(context)

        clientIdFingerprints.executePatch { patchClientId(context) }
        userAgentFingerprints.executePatch { patchUserAgent(context) }
        miscellaneousFingerprints.executePatch { patchMiscellaneous(context) }
    }

    /**
     * Patch the client ID.
     * The fingerprints are guaranteed to be in the same order as in [clientIdFingerprints].
     *
     * @param context The current [BytecodeContext].
     *
     */
    abstract fun List<MethodFingerprintResult>.patchClientId(context: BytecodeContext)

    /**
     * Patch the user agent.
     * The fingerprints are guaranteed to be in the same order as in [userAgentFingerprints].
     *
     * @param context The current [BytecodeContext].
     */
    // Not every client needs to patch the user agent.
    open fun List<MethodFingerprintResult>.patchUserAgent(context: BytecodeContext) {}

    /**
     * Patch miscellaneous things such as protection measures.
     * The fingerprints are guaranteed to be in the same order as in [miscellaneousFingerprints].
     *
     * @param context The current [BytecodeContext].
     */
    // Not every client needs to patch miscellaneous things.
    open fun List<MethodFingerprintResult>.patchMiscellaneous(context: BytecodeContext) {}
}