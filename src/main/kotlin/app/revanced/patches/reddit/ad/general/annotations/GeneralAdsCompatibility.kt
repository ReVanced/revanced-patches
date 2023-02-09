package app.revanced.patches.reddit.ad.general.annotations

import app.revanced.patcher.annotation.Compatibility
import app.revanced.patcher.annotation.Package

@Compatibility(
    [Package(
        "com.reddit.frontpage", arrayOf("2022.43.0", "2023.05.0")
    )]
)
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
internal annotation class GeneralAdsCompatibility
