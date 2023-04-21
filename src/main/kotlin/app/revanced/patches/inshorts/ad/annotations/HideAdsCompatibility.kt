package app.revanced.patches.inshorts.ad.annotations

import app.revanced.patcher.annotation.Compatibility
import app.revanced.patcher.annotation.Package

@Compatibility([Package("com.nis.app")])

@Target(AnnotationTarget.CLASS)
internal annotation class HideAdsCompatibility
