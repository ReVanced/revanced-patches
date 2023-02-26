package app.revanced.patches.music.misc.microg.annotations

import app.revanced.patcher.annotation.Compatibility
import app.revanced.patcher.annotation.Package

@Compatibility([Package("com.google.android.apps.youtube.music")])
@Target(AnnotationTarget.CLASS)
internal annotation class MusicMicroGPatchCompatibility
