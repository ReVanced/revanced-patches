package app.revanced.patches.trakt.annotations

import app.revanced.patcher.annotation.Compatibility
import app.revanced.patcher.annotation.Package

@Compatibility([Package("tv.trakt.trakt", arrayOf("1.1.1"))])
@Target(AnnotationTarget.CLASS)
internal annotation class UnlockProCompatibility
