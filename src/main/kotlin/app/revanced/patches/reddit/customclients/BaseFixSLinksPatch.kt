package app.revanced.patches.reddit.customclients

import app.revanced.patcher.PatchClass
import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.fingerprint.MethodFingerprint
import app.revanced.patcher.fingerprint.MethodFingerprintResult
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.util.resultOrThrow

abstract class BaseFixSLinksPatch(
    private val handleNavigationFingerprint: MethodFingerprint,
    private val setAccessTokenFingerprint: MethodFingerprint,
    compatiblePackages: Set<CompatiblePackage>,
    dependencies: Set<PatchClass> = emptySet(),
) : BytecodePatch(
    name = "Fix /s/ links",
    fingerprints = setOf(handleNavigationFingerprint, setAccessTokenFingerprint),
    compatiblePackages = compatiblePackages,
    dependencies = dependencies,
) {
    protected abstract val integrationsClassDescriptor: String

    protected val resolveSLinkMethod =
        "patchResolveSLink(Ljava/lang/String;)Z"

    protected val setAccessTokenMethod =
        "patchSetAccessToken(Ljava/lang/String;)V"

    override fun execute(context: BytecodeContext) {
        handleNavigationFingerprint.resultOrThrow().patchNavigationHandler(context)
        setAccessTokenFingerprint.resultOrThrow().patchSetAccessToken(context)
    }

    /**
     * Patch app's navigation handler to resolve /s/ links.
     *
     * @param context The current [BytecodeContext].
     *
     */
    protected abstract fun MethodFingerprintResult.patchNavigationHandler(context: BytecodeContext)

    /**
     * Patch access token setup in app to resolve /s/ links with an access token
     * in order to bypass API bans when making unauthorized requests.
     *
     * @param context The current [BytecodeContext].
     */
    protected abstract fun MethodFingerprintResult.patchSetAccessToken(context: BytecodeContext)
}
