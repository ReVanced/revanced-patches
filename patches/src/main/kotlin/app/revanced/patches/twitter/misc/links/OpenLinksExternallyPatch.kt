package app.revanced.patches.twitter.misc.links

import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.patch.bytecodePatch
import app.revanced.patches.twitter.misc.extension.sharedExtensionPatch

@Suppress("unused")
val openLinksExternallyPatch = bytecodePatch(
    name = "Open links externally",
    description = "Changes links to always open in your external browser, instead of the in-app browser.",
    use = false,
) {
    dependsOn(sharedExtensionPatch)

    compatibleWith("com.twitter.android"("10.48.0-release.0"))

    execute {
        val methodReference =
            "Lapp/revanced/extension/twitter/patches/links/OpenLinksExternallyPatch;->" +
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
