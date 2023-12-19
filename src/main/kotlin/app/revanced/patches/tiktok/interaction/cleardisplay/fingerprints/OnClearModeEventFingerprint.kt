package app.revanced.patches.tiktok.interaction.cleardisplay.fingerprints

import app.revanced.patcher.fingerprint.MethodFingerprint

internal object OnClearModeEventFingerprint : MethodFingerprint(
    customFingerprint = { methodDef, _ ->
        methodDef.definingClass.endsWith("/ClearModePanelComponent;") && methodDef.name == "onClearModeEvent"
    }
)