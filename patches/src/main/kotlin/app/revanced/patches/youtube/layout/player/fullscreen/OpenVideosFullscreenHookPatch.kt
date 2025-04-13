package app.revanced.patches.youtube.layout.player.fullscreen

import app.revanced.patcher.patch.bytecodePatch
import app.revanced.patches.youtube.layout.shortsplayer.openShortsInRegularPlayerPatch
import app.revanced.patches.youtube.misc.extension.sharedExtensionPatch
import app.revanced.patches.youtube.misc.playservice.is_19_46_or_greater
import app.revanced.patches.youtube.misc.playservice.versionCheckPatch
import app.revanced.util.insertLiteralOverride

internal const val EXTENSION_CLASS_DESCRIPTOR =
    "Lapp/revanced/extension/youtube/patches/OpenVideosFullscreenHookPatch;"

/**
 * Used by both [openVideosFullscreenPatch] and [openShortsInRegularPlayerPatch].
 */
internal val openVideosFullscreenHookPatch = bytecodePatch {
    dependsOn(
        sharedExtensionPatch,
        versionCheckPatch
    )

    execute {
        if (!is_19_46_or_greater) {
            return@execute
        }

        openVideosFullscreenPortraitFingerprint.method.insertLiteralOverride(
            OPEN_VIDEOS_FULLSCREEN_PORTRAIT_FEATURE_FLAG,
            "$EXTENSION_CLASS_DESCRIPTOR->openVideoFullscreenPortrait(Z)Z"
        )
    }
}
