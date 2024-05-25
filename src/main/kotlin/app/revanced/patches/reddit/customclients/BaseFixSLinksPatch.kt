package app.revanced.patches.reddit.customclients

import app.revanced.patcher.PatchClass
import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.fingerprint.MethodFingerprint
import app.revanced.patcher.fingerprint.MethodFingerprintResult
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.util.exception

abstract class BaseFixSLinksPatch(
    private val navigationFingerprint: Set<MethodFingerprint> = emptySet(),
    private val setAccessTokenFingerprint: Set<MethodFingerprint> = emptySet(),
    compatiblePackages: Set<CompatiblePackage>,
    dependencies: Set<PatchClass> = emptySet(),
) : BytecodePatch(
    name = "Fix /s/ links",
    description = "Fixes the issue where /s/ links do not work.",
    fingerprints = buildSet {
        addAll(navigationFingerprint)
        addAll(setAccessTokenFingerprint)
    },
    compatiblePackages = compatiblePackages,
    dependencies = dependencies
) {

    override fun execute(context: BytecodeContext) {
        fun Set<MethodFingerprint>.executePatch(
            patch: Set<MethodFingerprintResult>.(BytecodeContext) -> Unit
        ) = this.map { it.result ?: throw it.exception }.toSet().patch(context)

        navigationFingerprint.executePatch { patchNavigation(context) }
        setAccessTokenFingerprint.executePatch { patchSetAccessToken(context) }
    }

    /**
     * Patch app's navigation handling.
     * The fingerprints are guaranteed to be in the same order as in [navigationFingerprint].
     *
     * @param context The current [BytecodeContext].
     *
     */
    open fun Set<MethodFingerprintResult>.patchNavigation(context: BytecodeContext) {}

    /**
     * Patch access token setup in app.
     * The fingerprints are guaranteed to be in the same order as in [setAccessTokenFingerprint].
     *
     * @param context The current [BytecodeContext].
     */
    open fun Set<MethodFingerprintResult>.patchSetAccessToken(context: BytecodeContext) {}

}