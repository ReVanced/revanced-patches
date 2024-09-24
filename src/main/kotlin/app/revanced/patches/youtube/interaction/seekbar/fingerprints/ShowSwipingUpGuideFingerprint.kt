package app.revanced.patches.youtube.interaction.seekbar.fingerprints

import app.revanced.patcher.fingerprint.MethodFingerprint
import app.revanced.util.containsWideLiteralInstructionValue
import com.android.tools.smali.dexlib2.AccessFlags

internal object ShowSwipingUpGuideFingerprint : MethodFingerprint(
    accessFlags = AccessFlags.FINAL.value,
    returnType = "Z",
    parameters = emptyList(),
    customFingerprint = { methodDef, _ ->
        methodDef.containsWideLiteralInstructionValue(1L)
    }
)