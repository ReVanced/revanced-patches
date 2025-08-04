package app.revanced.patches.youtube.video.audio

import app.revanced.patcher.fingerprint
import app.revanced.util.containsLiteralInstruction
import com.android.tools.smali.dexlib2.AccessFlags

internal val formatStreamModelToStringFingerprint = fingerprint {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
    returns("Ljava/lang/String;")
    custom { method, classDef ->
        method.name == "toString" && classDef.type ==
                "Lcom/google/android/libraries/youtube/innertube/model/media/FormatStreamModel;"
    }
}

internal const val AUDIO_STREAM_IGNORE_DEFAULT_FEATURE_FLAG = 45666189L

internal val selectAudioStreamFingerprint = fingerprint {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.STATIC)
    returns("L")
    custom { method, _ ->
        method.parameters.size > 2 // Method has a large number of parameters and may change.
                && method.parameters[1].type == "Lcom/google/android/libraries/youtube/innertube/model/media/PlayerConfigModel;"
                && method.containsLiteralInstruction(AUDIO_STREAM_IGNORE_DEFAULT_FEATURE_FLAG)
    }
}

