package app.revanced.patches.twitter.misc.links

import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.annotation.CompatiblePackage
import app.revanced.patcher.patch.annotation.Patch
import app.revanced.patches.twitter.misc.links.fingerprints.OpenLinkFingerprint
import app.revanced.util.exception

@Patch(
    name = "Open links with app chooser",
    description = "Instead of opening links directly, open them with an app chooser. " +
        "As a result you can select a browser to open the link with.",
    compatiblePackages = [CompatiblePackage("com.twitter.android")],
    use = false,
)
@Suppress("unused")
object OpenLinksWithAppChooserPatch : BytecodePatch(
    setOf(OpenLinkFingerprint),
) {
    private const val METHOD_REFERENCE =
        "Lapp/revanced/integrations/twitter/patches/links/OpenLinksWithAppChooserPatch;->" +
            "openWithChooser(Landroid/content/Context;Landroid/content/Intent;)V"

    override fun execute(context: BytecodeContext) {
        OpenLinkFingerprint.result?.mutableMethod?.addInstructions(
            0,
            """
                invoke-static { p0, p1 }, $METHOD_REFERENCE
                return-void
            """,
        ) ?: throw OpenLinkFingerprint.exception
    }
}
