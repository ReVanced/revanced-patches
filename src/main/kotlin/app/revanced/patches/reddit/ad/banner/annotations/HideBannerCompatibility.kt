package app.revanced.patches.reddit.ad.banner.annotations

import app.revanced.patcher.annotation.Compatibility
import app.revanced.patcher.annotation.Package

@Compatibility(
    [Package(
        "com.reddit.frontpage", arrayOf(
            "2023.05.0",
            "2023.06.0",
            "2023.07.0", 
            "2023.07.1",
            "2023.08.0",
            "2023.09.0",
            "2023.09.1",
            "2023.10.0",
            "2023.11.0",
            "2023.12.0"
        )
    )]
)
@Target(AnnotationTarget.CLASS)
internal annotation class HideBannerCompatibility
