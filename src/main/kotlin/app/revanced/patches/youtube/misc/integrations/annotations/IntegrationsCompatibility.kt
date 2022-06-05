package app.revanced.patches.youtube.misc.integrations.annotations

import app.revanced.patcher.annotation.Compatibility
import app.revanced.patcher.annotation.Package

@Compatibility(
    [Package(
        "com.google.android.youtube", arrayOf("17.03.38", "17.14.35", "17.17.34", "17.19.36", "17.20.37")
    )]
)
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
internal annotation class IntegrationsCompatibility