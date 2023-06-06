package app.revanced.patches.songpal.badge.annotations

import app.revanced.patcher.annotation.Compatibility
import app.revanced.patcher.annotation.Package

@Compatibility([Package("com.sony.songpal.mdr")])
@Target(AnnotationTarget.CLASS)
internal annotation class BadgeCompatibility
