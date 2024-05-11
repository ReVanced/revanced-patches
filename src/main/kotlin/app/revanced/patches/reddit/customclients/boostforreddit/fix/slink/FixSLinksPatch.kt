package app.revanced.patches.reddit.customclients.boostforreddit.fix.slink

import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.annotation.CompatiblePackage
import app.revanced.patcher.patch.annotation.Patch
import app.revanced.patches.reddit.customclients.boostforreddit.fix.slink.fingerprints.NavigationFingerprint
import app.revanced.util.exception

@Patch(
    name = "Fix /s/ links",
    description = "Fixes the issue where /s/ links do not work.",
    compatiblePackages = [
        CompatiblePackage("com.rubenmayayo.reddit")
    ],
    requiresIntegrations = true
)
object FixSLinksPatch : BytecodePatch(
    setOf(NavigationFingerprint)
) {
    override fun execute(context: BytecodeContext) =
        NavigationFingerprint.result?.mutableMethod?.addInstructions(
            1,
            """
                invoke-static { p0, p1 }, Lapp/revanced/integrations/boostforreddit/FixSLinksPatch;->resolveSLink(Landroid/content/Context;Ljava/lang/String;)Ljava/lang/String;
                move-result-object p1
            """
        ) ?: throw NavigationFingerprint.exception
}
