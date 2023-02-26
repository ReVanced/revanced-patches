package app.revanced.patches.windyapp.misc.pro.annotations

import app.revanced.patcher.annotation.Compatibility
import app.revanced.patcher.annotation.Package

@Compatibility([Package("co.windyapp.android")])
@Target(AnnotationTarget.CLASS)
internal annotation class UnlockProCompatibility
