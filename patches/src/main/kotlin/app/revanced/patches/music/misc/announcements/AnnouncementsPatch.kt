package app.revanced.patches.music.misc.announcements

import app.revanced.patches.all.misc.announcements.announcementsPatch
import app.revanced.patches.music.shared.musicActivityOnCreateFingerprint

val announcementsPatch = announcementsPatch(
    musicActivityOnCreateFingerprint,
    {
        compatibleWith("com.google.android.apps.music")
    },
)
