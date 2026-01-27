package app.revanced.patches.nothingx.misc.logk1token

import app.revanced.patcher.extensions.InstructionExtensions.addInstruction
import app.revanced.patcher.patch.bytecodePatch
import app.revanced.patches.nothingx.misc.extension.sharedExtensionPatch

private const val EXTENSION_CLASS_DESCRIPTOR =
    "Lapp/revanced/extension/nothingx/patches/ShowK1TokensPatch;"

@Suppress("unused")
val showK1TokensPatch = bytecodePatch(
    name = "Show K1 token(s)",
    description = "Shows the K1 authentication token(s) in a dialog and logs it to logcat " +
            "for pairing with GadgetBridge without requiring root access. " +
            "After installing this patch, pair your watch  with the Nothing X app and " +
            "use the token from the dialog or logcat.",
) {
    dependsOn(sharedExtensionPatch)

    compatibleWith("com.nothing.smartcenter"())

    execute {
        // Hook Application.onCreate to get K1 tokens from database and log files.
        // This will find K1 tokens that were already written to log files.
        // p0 is the Application context in onCreate.
        applicationOnCreateFingerprint.method.addInstruction(
            0,
            "invoke-static { p0 }, $EXTENSION_CLASS_DESCRIPTOR->showK1Tokens(Landroid/content/Context;)V",
        )
    }
}
