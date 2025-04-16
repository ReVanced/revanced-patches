package app.revanced.patches.youtube.misc.dns

import app.revanced.patcher.extensions.InstructionExtensions.addInstruction
import app.revanced.patcher.patch.bytecodePatch
import app.revanced.patches.all.misc.resources.addResources
import app.revanced.patches.all.misc.resources.addResourcesPatch
import app.revanced.patches.youtube.misc.extension.sharedExtensionPatch
import app.revanced.patches.youtube.shared.mainActivityOnCreateFingerprint

private const val EXTENSION_CLASS_DESCRIPTOR =
    "Lapp/revanced/extension/youtube/patches/CheckWatchHistoryDomainNameResolutionPatch;"

val checkWatchHistoryDomainNameResolutionPatch = bytecodePatch(
    name = "Check watch history domain name resolution",
    description = "Checks if the device DNS server is preventing user watch history from being saved.",
) {
    dependsOn(
        sharedExtensionPatch,
        addResourcesPatch
    )

    compatibleWith(
        "com.google.android.youtube"(
            "19.16.39",
            "19.25.37",
            "19.34.42",
            "19.43.41",
            "19.47.53",
            "20.07.39",
            "20.12.46",
        )
    )

    execute {
        addResources("youtube", "misc.dns.checkWatchHistoryDomainNameResolutionPatch")

        mainActivityOnCreateFingerprint.method.addInstruction(
            // FIXME: Insert index must be greater than the insert index used by GmsCoreSupport,
            //  as both patch the same method and GmsCoreSupport check should be first,
            //  but the patch does not depend on GmsCoreSupport, so it should not be possible to enforce this
            //  unless a third patch is added that this patch and GmsCoreSupport depend on to manage
            //  the order of the patches.
            1,
            "invoke-static/range { p0 .. p0 }, $EXTENSION_CLASS_DESCRIPTOR->checkDnsResolver(Landroid/app/Activity;)V",
        )
    }
}
