package app.revanced.patches.twitch.chat.antidelete.annotations

import app.revanced.patcher.annotation.Compatibility
import app.revanced.patcher.annotation.Package

@Compatibility([Package("tv.twitch.android.app", arrayOf("15.4.1"))])
@Target(AnnotationTarget.CLASS)
internal annotation class ShowDeletedMessagesCompatibility

