package app.revanced.patches.youtube.video.audio

import app.revanced.patcher.fingerprint
import app.revanced.patcher.string
import com.android.tools.smali.dexlib2.AccessFlags

internal val streamingModelBuilderFingerprint by fingerprint {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
    returns("L")
    instructions(
        string("vprng")
    )
}

internal val menuItemAudioTrackFingerprint by fingerprint {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
    parameters("L")
    returns("V")
    instructions(
        string("menu_item_audio_track")
    )
}

internal val audioStreamingTypeSelector by fingerprint {
    accessFlags(AccessFlags.PRIVATE, AccessFlags.FINAL)
    returns("L")
    instructions(
        string("raw") // String is not unique
    )
}