package app.revanced.patches.youtube.video.videoid

import app.revanced.patcher.*
import app.revanced.patcher.patch.BytecodePatchContext
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.iface.ClassDef

/**
 * Matches using the class found in [videoIdParentMethodMatch].
 */
context(_: BytecodePatchContext)
internal fun ClassDef.getVideoIdMethodMatch() = firstMethodComposite {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
    returnType("V")
    parameterTypes("L")
    instructions(
        method {
            definingClass == "Lcom/google/android/libraries/youtube/innertube/model/player/PlayerResponseModel;" &&
                returnType == "Ljava/lang/String;"
        },
        Opcode.MOVE_RESULT_OBJECT(),
    )
}

internal val videoIdBackgroundPlayMethodMatch = firstMethodComposite {
    accessFlags(AccessFlags.DECLARED_SYNCHRONIZED, AccessFlags.FINAL, AccessFlags.PUBLIC)
    returnType("V")
    parameterTypes("L")
    instructions(
        method {
            definingClass == "Lcom/google/android/libraries/youtube/innertube/model/player/PlayerResponseModel;" &&
                returnType == "Ljava/lang/String;"
        },
        Opcode.MOVE_RESULT_OBJECT(),
        Opcode.IPUT_OBJECT(),
        Opcode.MONITOR_EXIT(),
        Opcode.RETURN_VOID(),
        Opcode.MONITOR_EXIT(),
        Opcode.RETURN_VOID(),
    )
    custom {
        immutableClassDef.methods.count() == 17 || // 20.39 and lower.
            immutableClassDef.methods.count() == 16 // 20.40+
    }
}

internal val videoIdParentMethodMatch = firstMethodComposite {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
    returnType("[L")
    parameterTypes("L")
    instructions(
        524288L(),
    )
}
