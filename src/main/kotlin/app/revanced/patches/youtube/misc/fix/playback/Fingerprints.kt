package app.revanced.patches.youtube.misc.fix.playback

import com.android.tools.smali.dexlib2.iface.reference.MethodReference
import app.revanced.util.getReference
import app.revanced.util.containsWideLiteralInstructionValue
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.iface.reference.FieldReference
import app.revanced.util.literal
import app.revanced.util.indexOfFirstInstruction
import com.android.tools.smali.dexlib2.iface.Method
import com.android.tools.smali.dexlib2.AccessFlags
import app.revanced.patcher.fingerprint.methodFingerprint

internal val buildInitPlaybackRequestFingerprint = methodFingerprint {
    returns("Lorg/chromium/net/UrlRequest\$Builder;")
    opcodes(
        Opcode.MOVE_RESULT_OBJECT,
        Opcode.IGET_OBJECT, // Moves the request URI string to a register to build the request with.
    )
    strings(
        "Content-Type",
        "Range",
    )
}

internal val buildPlayerRequestURIFingerprint = methodFingerprint {
    returns("Ljava/lang/String;")
    opcodes(
        Opcode.INVOKE_VIRTUAL, // Register holds player request URI.
        Opcode.MOVE_RESULT_OBJECT,
        Opcode.IPUT_OBJECT,
        Opcode.IGET_OBJECT,
        Opcode.MONITOR_EXIT,
        Opcode.RETURN_OBJECT,
    )
    strings(
        "youtubei/v1",
        "key",
        "asig",
    )
}

internal val createPlaybackSpeedMenuItemFingerprint = methodFingerprint {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
    returns("V")
    opcodes(
        Opcode.IGET_OBJECT, // First instruction of the method
        Opcode.IGET_OBJECT,
        Opcode.IGET_OBJECT,
        Opcode.CONST_4,
        Opcode.IF_EQZ,
        Opcode.INVOKE_INTERFACE,
        null, // MOVE_RESULT or MOVE_RESULT_OBJECT, Return value controls the creation of the playback speed menu item.
    )
    // 19.01 and earlier is missing the second parameter.
    // Since this fingerprint is somewhat weak, work around by checking for both method parameter signatures.
    custom { methodDef, _ ->
        // 19.01 and earlier parameters are: "[L"
        // 19.02+ parameters are "[L", "F"
        val parameterTypes = methodDef.parameterTypes
        val firstParameter = parameterTypes.firstOrNull()

        if (firstParameter == null || !firstParameter.startsWith("[L")) {
            return@custom false
        }

        parameterTypes.size == 1 || (parameterTypes.size == 2 && parameterTypes[1] == "F")
    }
}

internal val createPlayerRequestBodyFingerprint = methodFingerprint {
    returns("V")
    parameters("L")
    opcodes(
        Opcode.CHECK_CAST,
        Opcode.IGET,
        Opcode.AND_INT_LIT16,
    )
    strings("ms")
}

internal fun indexOfBuildModelInstruction(methodDef: Method) =
    methodDef.indexOfFirstInstruction {
        val reference = getReference<FieldReference>()
        reference?.definingClass == "Landroid/os/Build;" &&
            reference.name == "MODEL" &&
            reference.type == "Ljava/lang/String;"
    }

internal val createPlayerRequestBodyWithModelFingerprint = methodFingerprint {
    returns("L")
    accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
    parameters()
    custom { methodDef, _ ->
        methodDef.containsWideLiteralInstructionValue(1073741824) && indexOfBuildModelInstruction(methodDef) >= 0
    }
}

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

internal val setPlayerRequestClientTypeFingerprint = methodFingerprint {
    opcodes(
        Opcode.IGET,
        Opcode.IPUT, // Sets ClientInfo.clientId.
    )
    strings("10.29")
    literal { 134217728 }
}
