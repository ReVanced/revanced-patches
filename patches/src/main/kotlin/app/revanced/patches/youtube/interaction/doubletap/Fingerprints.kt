package app.revanced.patches.youtube.interaction.doubletap

import app.revanced.patcher.fingerprint
import com.android.tools.smali.dexlib2.AccessFlags

internal val chapterSeekResultToStringFingerprint = fingerprint {
    parameters()
    returns("Ljava/lang/String;")
    strings("ChapterSeekResult{isSeekingToChapterStart=")
    custom { method, _ ->
        method.name == "toString"
    }
}

internal val chapterSeekResultCtorFingerprint = fingerprint {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.CONSTRUCTOR)
    parameters(
        "Z",
        "Lj\$/time/Duration;",
        "Lj\$/util/Optional;",
        "Z",
    )
}
