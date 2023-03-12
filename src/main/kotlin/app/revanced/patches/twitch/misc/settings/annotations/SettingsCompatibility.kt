package app.revanced.patches.twitch.misc.settings.annotations

import app.revanced.patcher.annotation.Compatibility
import app.revanced.patcher.annotation.Package

mpatibility([Package("tv.twitch.android.app")])
@Target(AnnotationTarget.CLASS)
internal annotation class SettingsCompatibility
