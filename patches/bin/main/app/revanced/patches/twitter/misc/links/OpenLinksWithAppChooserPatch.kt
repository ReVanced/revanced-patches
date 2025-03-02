package app.revanced.patches.twitter.misc.links

import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.patch.bytecodePatch
import app.revanced.patches.twitter.misc.extension.sharedExtensionPatch

@Suppress("unused")
val openLinksWithAppChooserPatch = bytecodePatch(
    name = "Open links with app chooser",
    description = "Instead of opening links directly, open them with an app chooser. " +
        "As a result you can select a browser to open the link with.",
    use = false,
) {
    dependsOn(sharedExtensionPatch)

    compatibleWith("com.twitter.android"("10.48.0-release.0"))

    execute {
        val methodReference =
            "Lapp/revanced/extension/twitter/patches/links/OpenLinksWithAppChooserPatch;->" +
                "openWithChooser(Landroid/content/Context;Landroid/content/Intent;)V"

        openLinkFingerprint.method.addInstructions(
            0,
            """
                invoke-static { p0, p1 }, $methodReference
                return-void
            """,
        )
    }
}
