package app.revanced.patches.youtube.misc.announcements

import app.revanced.patches.all.misc.announcements.announcementsPatch
import app.revanced.patches.all.misc.resources.addResources
import app.revanced.patches.music.misc.extension.sharedExtensionPatch
import app.revanced.patches.shared.misc.settings.preference.SwitchPreference
import app.revanced.patches.youtube.misc.settings.PreferenceScreen
import app.revanced.patches.youtube.misc.settings.settingsPatch
import app.revanced.patches.youtube.shared.mainActivityOnCreateFingerprint

val announcementsPatch = announcementsPatch(
    mainActivityOnCreateFingerprint,
    sharedExtensionPatch,
    {
        dependsOn(settingsPatch)

        compatibleWith("com.google.android.youtube")
    },
    {
        addResources("youtube", "misc.announcements.announcementsPatch")

        PreferenceScreen.MISC.addPreferences(
            SwitchPreference("revanced_announcements"),
        )
    },
)
