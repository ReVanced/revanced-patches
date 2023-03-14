package app.revanced.patches.iconpackstudio.misc.pro.annotations

import app.revanced.patcher.annotation.Compatibility
import app.revanced.patcher.annotation.Package

@Compatibility([Package("ginlemon.iconpackstudio")])
@Target(AnnotationTarget.CLASS)
internal annotation class UnlockProCompatibility
