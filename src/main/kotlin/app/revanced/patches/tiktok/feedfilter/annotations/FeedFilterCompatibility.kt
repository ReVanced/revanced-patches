package app.revanced.patches.tiktok.feedfilter.annotations

import app.revanced.patcher.annotation.Compatibility
import app.revanced.patcher.annotation.Package

@Compatibility(
    [
        Package("com.ss.android.ugc.trill", arrayOf("27.8.3")),
        Package("com.zhiliaoapp.musically", arrayOf("27.8.3"))
    ]
)
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
internal annotation class FeedFilterCompatibility
