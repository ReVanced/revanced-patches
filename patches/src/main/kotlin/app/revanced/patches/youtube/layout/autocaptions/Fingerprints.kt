package app.revanced.patches.youtube.layout.autocaptions

import app.revanced.patcher.accessFlags
import app.revanced.patcher.gettingFirstMethodDeclaratively
import app.revanced.patcher.opcodes
import app.revanced.patcher.parameterTypes
import app.revanced.patcher.patch.BytecodePatchContext
import app.revanced.patcher.returnType
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.Opcode

internal val BytecodePatchContext.startVideoInformerMethod by gettingFirstMethodDeclaratively {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
    returnType("V")
    opcodes(
        Opcode.INVOKE_INTERFACE,
        Opcode.RETURN_VOID,
    )
    strings("pc")
}

internal val BytecodePatchContext.storyboardRendererDecoderRecommendedLevelMethod by gettingFirstMethodDeclaratively {
    returnType("V")
    accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
    parameterTypes("L")
    strings("#-1#")
}

internal val BytecodePatchContext.subtitleTrackMethod by gettingFirstMethodDeclaratively {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
    returnType("Z")
    parameterTypes()
    opcodes(
        Opcode.CONST_STRING,
        Opcode.INVOKE_VIRTUAL,
        Opcode.MOVE_RESULT_OBJECT,
        Opcode.INVOKE_VIRTUAL,
        Opcode.MOVE_RESULT,
        Opcode.RETURN,
    )
    strings("DISABLE_CAPTIONS_OPTION")
    custom { _, classDef ->
        classDef.endsWith("/SubtitleTrack;")
    }
}
