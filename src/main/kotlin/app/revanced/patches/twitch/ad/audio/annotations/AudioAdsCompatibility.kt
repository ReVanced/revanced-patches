package app.revanced.patches.twitch.ad.audio.annotations

import app.revanced.patcher.annotation.Compatibility
import app.revanced.patcher.annotation.Package

@Compatibility(
    [
        Package(
            "tv.twitch.android.app", arrayOf(
                "14.3.3",
                "14.4.0",
                "14.5.0", 
                "14.5.2",
                "14.6.0",
                "14.6.1"
            )
        )
    ]
)
@Target(AnnotationTarget.CLASS)
internal annotation class AudioAdsCompatibility

