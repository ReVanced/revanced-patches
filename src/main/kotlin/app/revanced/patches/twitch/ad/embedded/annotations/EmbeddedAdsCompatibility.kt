package app.revanced.patches.twitch.ad.embedded.annotations

import app.revanced.patcher.annotation.Compatibility
import app.revanced.patcher.annotation.Package

@Compatibility([Package("tv.twitch.android.app")])
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
internal annotation class EmbeddedAdsCompatibility

