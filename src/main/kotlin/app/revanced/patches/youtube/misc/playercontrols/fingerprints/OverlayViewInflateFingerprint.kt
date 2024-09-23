package app.revanced.patches.youtube.misc.playercontrols.fingerprints

import app.revanced.patcher.extensions.or
import app.revanced.patcher.fingerprint.MethodFingerprint
import app.revanced.patches.youtube.misc.playercontrols.PlayerControlsResourcePatch
import app.revanced.util.containsWideLiteralInstructionValue
import com.android.tools.smali.dexlib2.AccessFlags

internal object OverlayViewInflateFingerprint : MethodFingerprint(
    accessFlags = AccessFlags.PUBLIC or AccessFlags.FINAL,
    returnType = "V",
    parameters = listOf("Landroid/view/View;"),
    customFingerprint = { methodDef, _ ->
        methodDef.containsWideLiteralInstructionValue(PlayerControlsResourcePatch.fullscreenButton) &&
                methodDef.containsWideLiteralInstructionValue(PlayerControlsResourcePatch.heatseekerViewstub)
    }
)