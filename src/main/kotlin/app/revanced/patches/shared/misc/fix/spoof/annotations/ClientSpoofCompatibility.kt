package app.revanced.patches.shared.misc.fix.spoof.annotations

import app.revanced.patcher.annotation.Compatibility
import app.revanced.patcher.annotation.Package

@Compatibility(
    [
        Package("com.google.android.youtube"),
        Package("com.vanced.android.youtube")
    ]
)
@Target(AnnotationTarget.CLASS)
internal annotation class ClientSpoofCompatibility
