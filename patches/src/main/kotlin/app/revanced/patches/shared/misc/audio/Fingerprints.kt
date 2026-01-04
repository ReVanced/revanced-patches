package app.revanced.patches.shared.misc.audio

import app.revanced.patcher.fingerprint
import app.revanced.patcher.literal
import com.android.tools.smali.dexlib2.AccessFlags

internal val formatStreamModelToStringFingerprint = fingerprint {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
    returns("Ljava/lang/String;")
    custom { method, _ ->
        method.name == "toString"
    }
    strings(
        // Strings are partial matches.
        "isDefaultAudioTrack=",
        "audioTrackId="
    )
}

internal val selectAudioStreamFingerprint = fingerprint {
    instructions(
        literal(45666189L)
    )
}
