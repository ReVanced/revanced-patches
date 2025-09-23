package app.revanced.patches.shared.misc.dns

import app.revanced.patcher.Fingerprint
import app.revanced.patcher.extensions.InstructionExtensions.addInstruction
import app.revanced.patcher.patch.BytecodePatchBuilder
import app.revanced.patcher.patch.BytecodePatchContext
import app.revanced.patcher.patch.bytecodePatch
import app.revanced.patches.all.misc.resources.addResources

private const val EXTENSION_CLASS_DESCRIPTOR =
    "Lapp/revanced/extension/shared/patches/CheckWatchHistoryDomainNameResolutionPatch;"

/**
 * Patch shared with YouTube and YT Music.
 */
internal fun checkWatchHistoryDomainNameResolutionPatch(
    block: BytecodePatchBuilder.() -> Unit = {},
    executeBlock: BytecodePatchContext.() -> Unit = {},
    mainActivityFingerprint: Fingerprint
) = bytecodePatch(
    name = "Check watch history domain name resolution",
    description = "Checks if the device DNS server is preventing user watch history from being saved.",
) {
    block()

    execute {
        executeBlock()

        addResources("shared", "misc.dns.checkWatchHistoryDomainNameResolutionPatch")

        mainActivityFingerprint.method.addInstruction(
            0,
            "invoke-static/range { p0 .. p0 }, $EXTENSION_CLASS_DESCRIPTOR->checkDnsResolver(Landroid/app/Activity;)V",
        )
    }
}
