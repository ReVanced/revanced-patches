package app.revanced.patches.youtube.video.audio

import app.revanced.patcher.fingerprint
import com.android.tools.smali.dexlib2.AccessFlags

internal val formatStreamModelToString = fingerprint {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
    returns("Ljava/lang/String;")
    custom { method, classDef ->
        method.name == "toString" && classDef.type ==
                "Lcom/google/android/libraries/youtube/innertube/model/media/FormatStreamModel;"
    }
}
