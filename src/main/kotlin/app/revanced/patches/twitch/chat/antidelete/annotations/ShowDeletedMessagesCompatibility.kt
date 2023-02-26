package app.revanced.patches.twitch.chat.antidelete.annotations

import app.revanced.patcher.annotation.Compatibility
import app.revanced.patcher.annotation.Package

@Compatibility([Package("tv.twitch.android.app")])
@Target(AnnotationTarget.CLASS)
internal annotation class ShowDeletedMessagesCompatibility

