package app.revanced.patches.syncforreddit.ads.annotations

import app.revanced.patcher.annotation.Compatibility
import app.revanced.patcher.annotation.Package

@Compatibility([Package("com.laurencedawson.reddit_sync")])
@Target(AnnotationTarget.CLASS)
internal annotation class DisableAdsCompatibility
