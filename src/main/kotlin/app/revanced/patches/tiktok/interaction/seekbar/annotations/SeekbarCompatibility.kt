package app.revanced.patches.tiktok.interaction.seekbar.annotations

import app.revanced.patcher.annotation.Compatibility
import app.revanced.patcher.annotation.Package

@Compatibility(
    [
        Package("com.ss.android.ugc.trill", arrayOf()),
        Package("com.zhiliaoapp.musically", arrayOf())
    ]
)
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
internal annotation class SeekbarCompatibility
