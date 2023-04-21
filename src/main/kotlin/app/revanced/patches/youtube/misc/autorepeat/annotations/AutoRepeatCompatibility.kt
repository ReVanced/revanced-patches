package app.revanced.patches.youtube.misc.autorepeat.annotations

import app.revanced.patcher.annotation.Compatibility
import app.revanced.patcher.annotation.Package

@Compatibility(
    [Package(
        "com.google.android.youtube", arrayOf(
            "17.49.37",
            "18.03.36",
            "18.03.42",
            "18.04.35",
            "18.04.41",
            "18.05.32",
            "18.05.35",
            "18.05.40",
            "18.08.37"
        )
    )]
)
@Target(AnnotationTarget.CLASS)
internal annotation class AutoRepeatCompatibility