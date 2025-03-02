package app.revanced.patches.tiktok.interaction.cleardisplay

import app.revanced.patcher.fingerprint

internal val onClearDisplayEventFingerprint = fingerprint {
    custom { method, classDef ->
        // Internally the feature is called "Clear mode".
        classDef.endsWith("/ClearModePanelComponent;") && method.name == "onClearModeEvent"
    }
}
