package app.revanced.patches.all.misc.announcements

import app.revanced.patcher.Fingerprint
import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.patch.*
import app.revanced.patches.all.misc.resources.addResources
import app.revanced.patches.all.misc.resources.addResourcesPatch

private const val EXTENSION_CLASS_DESCRIPTOR =
    "Lapp/revanced/extension/shared/announcements/AnnouncementsPatch;"

fun announcementsPatch(
    mainActivityOnCreateFingerprint: Fingerprint,
    extensionPatch: Patch<*>,
    block: BytecodePatchBuilder.() -> Unit = {},
    executeBlock: BytecodePatchContext.() -> Unit = {},
) = bytecodePatch(
    name = "Announcements",
    description = "Adds an option to show announcements from ReVanced on app startup.",
) {
    block()

    dependsOn(
        extensionPatch,
        addResourcesPatch,
    )

    execute {
        addResources("shared", "misc.announcements.announcementsPatch")

        mainActivityOnCreateFingerprint.method.addInstructions(
            0,
            "invoke-static/range { p0 .. p0 }, $EXTENSION_CLASS_DESCRIPTOR->showAnnouncement(Landroid/app/Activity;)V",
        )

        executeBlock()
    }
}
