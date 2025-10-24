package app.revanced.patches.shared.misc.audio

import app.revanced.patcher.fingerprint
import app.revanced.util.literal
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

internal const val AUDIO_STREAM_IGNORE_DEFAULT_FEATURE_FLAG = 45666189L

internal val selectAudioStreamFingerprint = fingerprint {
    literal {
        AUDIO_STREAM_IGNORE_DEFAULT_FEATURE_FLAG
    }
}
