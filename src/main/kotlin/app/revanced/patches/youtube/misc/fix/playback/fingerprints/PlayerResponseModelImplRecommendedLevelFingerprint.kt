package app.revanced.patches.youtube.misc.fix.playback.fingerprints

import app.revanced.patcher.extensions.or
import app.revanced.patcher.fingerprint.MethodFingerprint
import app.revanced.util.containsWideLiteralInstructionValue
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.Opcode

@Deprecated("Fingerprint is obsolete and will be deleted soon")
internal object PlayerResponseModelImplRecommendedLevelFingerprint : MethodFingerprint(
    returnType = "I",
    accessFlags = AccessFlags.PUBLIC or AccessFlags.FINAL,
    parameters = emptyList(),
    opcodes = listOf(
        Opcode.SGET_OBJECT,
        Opcode.IGET,
        Opcode.RETURN,
    ),
    customFingerprint = handler@{ methodDef, _ ->
        if (!methodDef.definingClass.endsWith("/PlayerResponseModelImpl;")) return@handler false

        methodDef.containsWideLiteralInstructionValue(55735497)
    },
)
