package app.revanced.patches.youtube.misc.announcements

import app.revanced.patcher.extensions.InstructionExtensions.addInstruction
import app.revanced.patcher.patch.bytecodePatch
import app.revanced.patches.all.misc.resources.addResources
import app.revanced.patches.all.misc.resources.addResourcesPatch
import app.revanced.patches.shared.misc.settings.preference.SwitchPreference
import app.revanced.patches.youtube.misc.settings.PreferenceScreen
import app.revanced.patches.youtube.misc.settings.settingsPatch
import app.revanced.patches.youtube.shared.mainActivityOnCreateFingerprint

private const val EXTENSION_CLASS_DESCRIPTOR =
    "Lapp/revanced/extension/youtube/patches/announcements/AnnouncementsPatch;"

val announcementsPatch = bytecodePatch(
    name = "Announcements",
    description = "Adds an option to show announcements from ReVanced on app startup.",
) {
    dependsOn(
        settingsPatch,
        addResourcesPatch,
    )

    compatibleWith(
        "com.google.android.youtube"(
            "19.16.39",
            "19.25.37",
            "19.34.42",
            "19.43.41",
            "19.47.53",
            "20.07.39",
        ),
    )

    execute {
        addResources("youtube", "misc.announcements.announcementsPatch")

        PreferenceScreen.MISC.addPreferences(
            SwitchPreference("revanced_announcements"),
        )

        mainActivityOnCreateFingerprint.method.addInstruction(
            // Insert index must be greater than the insert index used by GmsCoreSupport,
            // as both patch the same method and GmsCore check should be first.
            1,
            "invoke-static/range { p0 .. p0 }, $EXTENSION_CLASS_DESCRIPTOR->showAnnouncement(Landroid/app/Activity;)V",
        )
    }
}
