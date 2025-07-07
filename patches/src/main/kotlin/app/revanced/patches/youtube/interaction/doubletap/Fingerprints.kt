package app.revanced.patches.youtube.interaction.doubletap

import app.revanced.patcher.fingerprint

internal val chapterSeekResultToStringFingerprint = fingerprint {
    parameters()
    returns("Ljava/lang/String;")
    strings("ChapterSeekResult{isSeekingToChapterStart=")
    custom { method, _ ->
        method.name == "toString"
    }
}

internal val chapterSeekResultCtorFingerprint = fingerprint {
    parameters(
        "Z",
        "Lj\$/time/Duration;",
        "Lj\$/util/Optional;",
        "Z",
    )
    custom { method, _ ->
        method.name == "<init>"
    }
}
