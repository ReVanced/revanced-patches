package app.revanced.patches.twitter.misc.links

import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.patch.bytecodePatch

@Suppress("unused")
val openLinksWithAppChooserPatch = bytecodePatch(
    name = "Open links with app chooser",
    description = "Instead of opening links directly, open them with an app chooser. " +
        "As a result you can select a browser to open the link with.",
    use = false,
) {
    compatibleWith("com.twitter.android")

    val openLinkResult by openLinkFingerprint

    execute {
        val methodReference =
            "Lapp/revanced/integrations/twitter/patches/links/OpenLinksWithAppChooserPatch;->" +
                "openWithChooser(Landroid/content/Context;Landroid/content/Intent;)V"

        openLinkResult.mutableMethod.addInstructions(
            0,
            """
                invoke-static { p0, p1 }, $methodReference
                return-void
            """,
        )
    }
}
