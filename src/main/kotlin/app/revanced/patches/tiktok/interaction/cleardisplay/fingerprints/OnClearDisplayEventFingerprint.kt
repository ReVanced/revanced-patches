package app.revanced.patches.tiktok.interaction.cleardisplay.fingerprints

import app.revanced.patcher.fingerprint.methodFingerprint

internal val onClearDisplayEventFingerprint = methodFingerprint {
    custom { methodDef, classDef ->
        // Internally the feature is called "Clear mode".
        classDef.endsWith("/ClearModePanelComponent;") && methodDef.name == "onClearModeEvent"
    }
}
