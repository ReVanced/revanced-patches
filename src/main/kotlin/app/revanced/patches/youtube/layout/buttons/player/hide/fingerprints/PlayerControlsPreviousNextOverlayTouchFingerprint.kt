package app.revanced.patches.youtube.layout.buttons.player.hide.fingerprints

import app.revanced.patcher.extensions.or
import app.revanced.patcher.fingerprint.MethodFingerprint
import app.revanced.patches.youtube.layout.buttons.player.hide.HidePlayerButtonsResourcePatch
import app.revanced.util.containsWideLiteralInstructionValue
import com.android.tools.smali.dexlib2.AccessFlags

internal object PlayerControlsPreviousNextOverlayTouchFingerprint : MethodFingerprint(
    accessFlags = AccessFlags.PUBLIC or AccessFlags.FINAL,
    returnType = "V",
    strings = listOf("1.0x"),
    customFingerprint = { methodDef, _ ->
        methodDef.containsWideLiteralInstructionValue(
            HidePlayerButtonsResourcePatch.playerControlPreviousButtonTouchArea
        ) && methodDef.containsWideLiteralInstructionValue(
            HidePlayerButtonsResourcePatch.playerControlNextButtonTouchArea
        )
    }
)