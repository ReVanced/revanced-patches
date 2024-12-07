package app.revanced.patches.music.misc.spoof

import app.revanced.patches.shared.misc.spoof.spoofVideoStreamsPatch

val spoofVideoStreamsPatch = spoofVideoStreamsPatch({
    compatibleWith("com.google.android.apps.youtube.music")
})
