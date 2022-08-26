package app.revanced.patches.youtube.misc.clientspoof.annotations

import app.revanced.patcher.annotation.Compatibility
import app.revanced.patcher.annotation.Package

@Compatibility(
    [
        Package("com.google.android.youtube", arrayOf()),
        Package("com.vanced.android.youtube", arrayOf())
    ]
)
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
internal annotation class ClientSpoofCompatibility
