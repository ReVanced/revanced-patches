package app.revanced.patches.youtube.interaction.doubletap

import app.revanced.patcher.fingerprint

internal val chapterSeekResultToStringFingerprint = fingerprint {
    parameters()
    returns("Ljava/lang/String;")
    strings(
        "ChapterSeekResult{isSeekingToChapterStart=",
        ", seekDuration=",
        ", seekText=",
        ", isOverlayCentered=",
        "}",
    )
    custom { method, _ ->
        method.name == "toString"
    }
}

internal val chapterSeekResultCtorFingerprint = fingerprint {
    custom { method, _ ->
        method.name == "<init>" && method.parameters.isNotEmpty()
    }
}
