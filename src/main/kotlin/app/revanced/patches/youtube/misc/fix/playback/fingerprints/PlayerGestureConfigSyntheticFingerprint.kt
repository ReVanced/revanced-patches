package app.revanced.patches.youtube.misc.fix.playback.fingerprints

import app.revanced.patcher.fingerprint.methodFingerprint
import app.revanced.util.getReference
import app.revanced.util.indexOfFirstInstruction
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.iface.Method
import com.android.tools.smali.dexlib2.iface.reference.MethodReference

internal val playerGestureConfigSyntheticFingerprint = methodFingerprint {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
    returns("V")
    parameters("Ljava/lang/Object;")
    opcodes(
        Opcode.SGET_OBJECT,
        Opcode.INVOKE_VIRTUAL,
        Opcode.MOVE_RESULT,
        Opcode.IF_EQZ,
        Opcode.IF_EQZ,
        Opcode.IGET_OBJECT,
        Opcode.INVOKE_INTERFACE,
        Opcode.MOVE_RESULT_OBJECT,
        Opcode.INVOKE_VIRTUAL, // playerGestureConfig.downAndOutLandscapeAllowed.
        Opcode.MOVE_RESULT,
        Opcode.CHECK_CAST,
        Opcode.IPUT_BOOLEAN,
        Opcode.INVOKE_INTERFACE,
        Opcode.MOVE_RESULT_OBJECT,
        Opcode.INVOKE_VIRTUAL, // playerGestureConfig.downAndOutPortraitAllowed.
        Opcode.MOVE_RESULT,
        Opcode.IPUT_BOOLEAN,
        Opcode.RETURN_VOID,
    )
    custom { methodDef, classDef ->
        fun indexOfDownAndOutAllowedInstruction(methodDef: Method) =
            methodDef.indexOfFirstInstruction {
                val reference = getReference<MethodReference>()
                reference?.definingClass == "Lcom/google/android/libraries/youtube/innertube/model/media/PlayerConfigModel;" &&
                    reference.parameterTypes.isEmpty() &&
                    reference.returnType == "Z"
            }

        // This method is always called "a" because this kind of class always has a single method.
        methodDef.name == "a" && classDef.methods.count() == 2 &&
            indexOfDownAndOutAllowedInstruction(methodDef) >= 0
    }
}
