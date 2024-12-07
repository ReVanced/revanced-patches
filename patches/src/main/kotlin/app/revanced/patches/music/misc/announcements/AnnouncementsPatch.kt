package app.revanced.patches.music.misc.announcements

import app.revanced.patches.all.misc.announcements.announcementsPatch
import app.revanced.patches.music.shared.musicActivityOnCreateFingerprint
import app.revanced.patches.youtube.misc.extension.sharedExtensionPatch

val announcementsPatch = announcementsPatch(
    musicActivityOnCreateFingerprint,
    sharedExtensionPatch,
    {
        compatibleWith("com.google.android.apps.music")
    },
)
