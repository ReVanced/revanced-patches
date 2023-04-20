package app.revanced.patches.photomath.misc.unlockplus.annotations

import app.revanced.patcher.annotation.Compatibility
import app.revanced.patcher.annotation.Package

@Compatibility(
    [Package(
        "com.microblink.photomath", arrayOf(
            "8.6.0",
            "8.7.0",
            "8.8.0",
            "8.9.0",
            "8.10.0",
            "8.11.0",
            "8.12.0",
            "8.13.0",
            "8.14.0",
            "8.15.0",
            "8.16.0",
            "8.17.0",
            "8.18.0",
            "8.18.1",
            "8.19.0",
            "8.20.0",
            "8.21.0",
        )
    )]
)
@Target(AnnotationTarget.CLASS)
internal annotation class UnlockPlusCompatibilty
