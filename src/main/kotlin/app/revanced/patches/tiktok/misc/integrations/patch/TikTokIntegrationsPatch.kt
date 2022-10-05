package app.revanced.patches.tiktok.misc.integrations.patch

import app.revanced.patcher.annotation.Description
import app.revanced.patcher.annotation.Name
import app.revanced.patcher.annotation.Version
import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.addInstruction
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.PatchResult
import app.revanced.patcher.patch.PatchResultError
import app.revanced.patcher.patch.PatchResultSuccess
import app.revanced.patches.tiktok.misc.integrations.annotations.TikTokIntegrationsCompatibility
import app.revanced.patches.tiktok.misc.integrations.fingerprints.InitFingerprint

@Name("tiktok-integrations")
@Description("Applies mandatory patches to implement the ReVanced integrations into the application.")
@TikTokIntegrationsCompatibility
@Version("0.0.1")
class TikTokIntegrationsPatch : BytecodePatch(
    listOf(
        InitFingerprint
    )
) {
    override fun execute(context: BytecodeContext): PatchResult {
        if (context.findClass("Lapp/revanced/tiktok/utils/ReVancedUtils") == null)
            return PatchResultError("Integrations have not been merged yet. This patch can not succeed without the integrations.")
        val result = InitFingerprint.result!!

        val method = result.mutableMethod
        val implementation = method.implementation!!
        val count = implementation.registerCount - 1

        method.addInstruction(
            0, "sput-object v$count, Lapp/revanced/tiktok/utils/ReVancedUtils;->context:Landroid/content/Context;"
        )
        return PatchResultSuccess()
    }
}