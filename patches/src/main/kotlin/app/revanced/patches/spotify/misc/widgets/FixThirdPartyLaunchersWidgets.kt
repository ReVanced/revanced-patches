package app.revanced.patches.spotify.misc.widgets

import app.revanced.patcher.patch.bytecodePatch
import app.revanced.util.returnEarly

@Suppress("unused")
val fixThirdPartyLaunchersWidgets = bytecodePatch(
    name = "Fix third party launchers widgets",
    description = "Fixes Spotify widgets not working in third party launchers, like Nova Launcher.",
) {
    compatibleWith("com.spotify.music")

    execute {
        // Only system app launchers are granted the BIND_APPWIDGET permission.
        // Override the method that checks for it to always return true, as this permission is not actually required
        // for the widgets to work.
        canBindAppWidgetPermissionFingerprint.method.returnEarly(true)
    }
}
