package app.revanced.patches.youtube.misc.announcements

import app.revanced.patcher.extensions.addInstruction
import app.revanced.patcher.patch.bytecodePatch
import app.revanced.patches.all.misc.resources.addResources
import app.revanced.patches.all.misc.resources.addResourcesPatch
import app.revanced.patches.shared.misc.settings.preference.SwitchPreference
import app.revanced.patches.youtube.misc.settings.PreferenceScreen
import app.revanced.patches.youtube.misc.settings.settingsPatch
import app.revanced.patches.youtube.shared.mainActivityOnCreateMethod

private const val EXTENSION_CLASS_DESCRIPTOR =
    "Lapp/revanced/extension/youtube/patches/announcements/AnnouncementsPatch;"

@Suppress("unused")
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
            "20.14.43",
            "20.21.37",
            "20.26.46",
            "20.31.42",
            "20.37.48",
            "20.40.45"
        ),
    )

    apply {
        addResources("youtube", "misc.announcements.announcementsPatch")

        PreferenceScreen.MISC.addPreferences(
            SwitchPreference("revanced_announcements"),
        )

        mainActivityOnCreateMethod.addInstruction(
            0,
            "invoke-static/range { p0 .. p0 }, $EXTENSION_CLASS_DESCRIPTOR->showAnnouncement(Landroid/app/Activity;)V",
        )
    }
}
