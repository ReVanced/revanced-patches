package app.revanced.patches.photomath.misc.unlockplus.annotations

import app.revanced.patcher.annotation.Compatibility
import app.revanced.patcher.annotation.Package

@Compatibility([Package("com.microblink.photomath", arrayOf("8.20.0"))])
@Target(AnnotationTarget.CLASS)
internal annotation class UnlockPlusCompatibilty
