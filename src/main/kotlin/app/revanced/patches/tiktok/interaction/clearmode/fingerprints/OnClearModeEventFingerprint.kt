package app.revanced.patches.tiktok.interaction.clearmode.fingerprints

import app.revanced.patcher.extensions.or
import app.revanced.patcher.fingerprint.MethodFingerprint
import com.android.tools.smali.dexlib2.AccessFlags

internal object OnClearModeEventFingerprint : MethodFingerprint(
    customFingerprint = { methodDef, _ ->
        methodDef.definingClass.endsWith("/ClearModePanelComponent;") &&
                methodDef.name == "onClearModeEvent"
    }
)