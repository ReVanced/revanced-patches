package app.revanced.patches.nova.prime.annotations

import app.revanced.patcher.annotation.Compatibility
import app.revanced.patcher.annotation.Package

@Compatibility(
    [
        Package("com.teslacoilsw.launcher")
    ]
)
@Target(AnnotationTarget.CLASS)
internal annotation class UnlockPrimeCompatibility