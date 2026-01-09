package app.revanced.patches.shared.misc.audio

import app.revanced.patcher.*
import com.android.tools.smali.dexlib2.AccessFlags

internal val formatStreamModelToStringMethodMatch = firstMethodComposite {
    name("toString")
    accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
    returnType("Ljava/lang/String;")
    instructions(
        "isDefaultAudioTrack="(String::contains),
        "audioTrackId="(String::contains)
    )
}

internal val selectAudioStreamMethodMatch = firstMethodComposite {
    instructions(45666189L())
}
