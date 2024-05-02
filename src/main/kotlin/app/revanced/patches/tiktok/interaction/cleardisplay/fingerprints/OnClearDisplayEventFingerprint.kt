package app.revanced.patches.tiktok.interaction.cleardisplay.fingerprints

import app.revanced.patcher.fingerprint.methodFingerprint

internal val onClearDisplayEventFingerprint = methodFingerprint {
    custom { methodDef, _ ->
        // Internally the feature is called "Clear mode".
        methodDef.definingClass.endsWith("/ClearModePanelComponent;") && methodDef.name == "onClearModeEvent"
    }
}
