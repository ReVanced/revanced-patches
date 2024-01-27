package app.revanced.patches.reddit.customclients

import app.revanced.patcher.PatchClass
import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.fingerprint.MethodFingerprint
import app.revanced.patcher.fingerprint.MethodFingerprintResult
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.options.PatchOption.PatchExtensions.stringPatchOption
import app.revanced.util.exception

abstract class BaseSpoofClientPatch(
    redirectUri: String,
    private val miscellaneousFingerprints: Set<MethodFingerprint> = emptySet(),
    private val clientIdFingerprints: Set<MethodFingerprint> = emptySet(),
    private val userAgentFingerprints: Set<MethodFingerprint> = emptySet(),
    compatiblePackages: Set<CompatiblePackage>,
    dependencies: Set<PatchClass> = emptySet(),
) : BytecodePatch(
    name = "Spoof client",
    description = "Restores functionality of the app by using custom client ID.",
    fingerprints = buildSet {
        addAll(clientIdFingerprints)
        userAgentFingerprints.let(::addAll)
        miscellaneousFingerprints.let(::addAll)
    },
    compatiblePackages = compatiblePackages,
    dependencies = dependencies
) {
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
        fun Set<MethodFingerprint>.executePatch(
            patch: Set<MethodFingerprintResult>.(BytecodeContext) -> Unit
        ) = this.map { it.result ?: throw it.exception }.toSet().patch(context)

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
    open fun Set<MethodFingerprintResult>.patchClientId(context: BytecodeContext) {}

    /**
     * Patch the user agent.
     * The fingerprints are guaranteed to be in the same order as in [userAgentFingerprints].
     *
     * @param context The current [BytecodeContext].
     */
    open fun Set<MethodFingerprintResult>.patchUserAgent(context: BytecodeContext) {}

    /**
     * Patch miscellaneous things such as protection measures.
     * The fingerprints are guaranteed to be in the same order as in [miscellaneousFingerprints].
     *
     * @param context The current [BytecodeContext].
     */
    open fun Set<MethodFingerprintResult>.patchMiscellaneous(context: BytecodeContext) {}
}