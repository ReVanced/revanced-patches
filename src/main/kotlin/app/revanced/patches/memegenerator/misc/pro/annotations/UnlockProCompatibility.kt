package app.revanced.patches.memegenerator.misc.pro.annotations

import app.revanced.patcher.annotation.Compatibility
import app.revanced.patcher.annotation.Package

@Compatibility(
    [Package(
        "com.zombodroid.MemeGenerator", arrayOf(
            "4.6364",
            "4.6370",
            "4.6375",
            "4.6377",
        )
    )]
)
@Target(AnnotationTarget.CLASS)
internal annotation class UnlockProCompatibility
