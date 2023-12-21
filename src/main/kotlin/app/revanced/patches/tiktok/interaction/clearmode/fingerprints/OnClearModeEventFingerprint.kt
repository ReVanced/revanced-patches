package app.revanced.patches.tiktok.interaction.clearmode.fingerprints

import app.revanced.patcher.fingerprint.MethodFingerprint

internal object OnClearModeEventFingerprint : MethodFingerprint(
    customFingerprint = { methodDef, _ ->
        methodDef.definingClass.endsWith("/ClearModePanelComponent;") && methodDef.name == "onClearModeEvent"
    }
)