package app.revanced.patches.youtubevanced.ad.general.annotations

import app.revanced.patcher.annotation.Compatibility
import app.revanced.patcher.annotation.Package

@Compatibility([Package("com.vanced.android.youtube")])
@Target(AnnotationTarget.CLASS)
internal annotation class HideAdsCompatibility
