package app.revanced.patches.youtube.misc.dnsresolver

import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.annotation.Patch
import app.revanced.patches.all.misc.resources.AddResourcesPatch
import app.revanced.patches.youtube.shared.fingerprints.MainActivityOnCreateFingerprint
import app.revanced.util.resultOrThrow

@Patch(
    description = "Checks, if the endpoint 's.youtube.com' to track watch history  is unreachable.",
)
@Suppress("unused")
internal object DnsResolverVerificationPatch : BytecodePatch(
    setOf(MainActivityOnCreateFingerprint)
) {
    private const val INTEGRATIONS_CLASS_DESCRIPTOR =
        "Lapp/revanced/integrations/youtube/patches/DnsResolverVerificationPatch;"

    override fun execute(context: BytecodeContext) {
        AddResourcesPatch(this::class)

        MainActivityOnCreateFingerprint.resultOrThrow().mutableMethod.addInstructions(
            // Insert index must be great than the insert index used by GmsCoreSupport,
            // as both patch the same method and GmsCore check should be first.
            1,
            "invoke-static/range { p0 .. p0 }, $INTEGRATIONS_CLASS_DESCRIPTOR->checkDnsResolver(Landroid/app/Activity;)V"
        )
    }
}
