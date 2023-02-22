package app.revanced.patches.photomath.misc.unlockplus.annotations

import app.revanced.patcher.annotation.Compatibility
import app.revanced.patcher.annotation.Package

@Compatibility([Package("com.microblink.photomath")])
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
internal annotation class UnlockPlusCompatibilty