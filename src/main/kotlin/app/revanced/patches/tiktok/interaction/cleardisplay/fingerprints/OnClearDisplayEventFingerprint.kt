package app.revanced.patches.tiktok.interaction.cleardisplay.fingerprints

import app.revanced.patcher.fingerprint.MethodFingerprint

internal object OnClearDisplayEventFingerprint : MethodFingerprint(
    customFingerprint = { methodDef, _ ->
        // Internally the feature is called "Clear mode".
        methodDef.definingClass.endsWith("/ClearModePanelComponent;") && methodDef.name == "onClearModeEvent"
    }
)