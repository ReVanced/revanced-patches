package app.revanced.patches.nyx.misc.pro.annotations

import app.revanced.patcher.annotation.Compatibility
import app.revanced.patcher.annotation.Package

@Compatibility([Package("com.awedea.nyx")])
@Target(AnnotationTarget.CLASS)
internal annotation class UnlockProCompatibility
