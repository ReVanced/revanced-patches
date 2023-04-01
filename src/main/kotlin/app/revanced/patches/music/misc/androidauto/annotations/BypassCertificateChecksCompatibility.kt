package app.revanced.patches.music.misc.androidauto.annotations

import app.revanced.patcher.annotation.Compatibility
import app.revanced.patcher.annotation.Package

@Compatibility(
    [Package(
        "com.google.android.apps.youtube.music",
        arrayOf(
            "5.14.53",
            "5.16.51",
            "5.17.51",
            "5.21.52",
            "5.22.54",
            "5.23.50",
            "5.25.51",
            "5.25.52",
            "5.26.52",
            "5.27.51",
            "5.28.52",
            "5.29.52",
            "5.31.50",
            "5.34.51",
            "5.36.51",
            "5.38.53",
            "5.39.52",
            "5.40.51",
            "5.41.50",
            "5.48.52"
        )
    )]
)
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
internal annotation class BypassCertificateChecksCompatibility
