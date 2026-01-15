package app.revanced.patches.nothingx.misc.logk1token

import app.revanced.patcher.extensions.InstructionExtensions.addInstruction
import app.revanced.patcher.patch.bytecodePatch
import app.revanced.patches.nothingx.misc.extension.extensionPatch

private const val EXTENSION_CLASS_DESCRIPTOR =
    "Lapp/revanced/extension/nothingx/patches/ExposeK1TokenPatch;"

/**
 * Patch to expose the K1 token for Nothing X app to adb logcat.
 *
 * This patch enables users to retrieve the K1 authentication token needed
 * to pair Nothing/CMF watches with GadgetBridge without requiring root access.
 *
 * The K1 token is normally written to internal log files that are inaccessible
 * without root. This patch makes the token visible in adb logcat during the
 * pairing process.
 */
val exposeK1TokenPatch = bytecodePatch(
    name = "Expose K1 token",
    description = "Logs the K1 authentication token visible to logcat for pairing with GadgetBridge. " +
        "After installing this patch, pair your watch with the Nothing X app, " +
        "filter the logs for 'NothingXKey' and copy the 32-character hex string to pair with GadgetBridge.",
) {
    dependsOn(extensionPatch)

    compatibleWith("com.nothing.smartcenter"("3.4.17"))

    execute {
        // Hook Application.onCreate to scan for existing log files.
        // This will find K1 tokens that were already written to log files.
        applicationOnCreateFingerprint.method.addInstruction(
            0,
            "invoke-static {}, $EXTENSION_CLASS_DESCRIPTOR->scanLogFilesForK1Token()V",
        )
    }
}