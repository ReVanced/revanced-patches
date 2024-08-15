package app.revanced.patches.youtube.misc.dns

import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.annotation.Patch
import app.revanced.patches.all.misc.resources.AddResourcesPatch
import app.revanced.patches.youtube.misc.integrations.IntegrationsPatch
import app.revanced.patches.youtube.shared.fingerprints.MainActivityOnCreateFingerprint
import app.revanced.util.resultOrThrow

@Patch(
    description = "Checks, if the endpoint 's.youtube.com' to track watch history  is unreachable.",
    dependencies = [IntegrationsPatch::class],
)
@Suppress("unused")
internal object CheckWatchHistoryDomainNameResolutionPatch : BytecodePatch(
    setOf(MainActivityOnCreateFingerprint),
) {
    private const val INTEGRATIONS_CLASS_DESCRIPTOR =
        "Lapp/revanced/integrations/youtube/patches/CheckWatchHistoryDomainNameResolutionPatch;"

    override fun execute(context: BytecodeContext) {
        AddResourcesPatch(this::class)

        MainActivityOnCreateFingerprint.resultOrThrow().mutableMethod.addInstructions(
            // FIXME: Insert index must be greater than the insert index used by GmsCoreSupport,
            //  as both patch the same method and GmsCoreSupport check should be first,
            //  but the patch does not depend on GmsCoreSupport, so it should not be possible to enforce this
            //  unless a third patch is added that this patch and GmsCoreSupport depend on to manage
            //  the order of the patches.
            1,
            "invoke-static/range { p0 .. p0 }, $INTEGRATIONS_CLASS_DESCRIPTOR->checkDnsResolver(Landroid/app/Activity;)V",
        )
    }
}
