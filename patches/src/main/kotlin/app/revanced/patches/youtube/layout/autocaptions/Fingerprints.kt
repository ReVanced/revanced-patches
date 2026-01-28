package app.revanced.patches.youtube.layout.autocaptions

import app.revanced.patcher.*
import app.revanced.patcher.patch.BytecodePatchContext
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.Opcode

internal val BytecodePatchContext.startVideoInformerMethod by gettingFirstMutableMethodDeclaratively("pc") {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
    returnType("V")
    opcodes(
        Opcode.INVOKE_INTERFACE,
        Opcode.RETURN_VOID,
    )
}

internal val BytecodePatchContext.storyboardRendererDecoderRecommendedLevelMethod by gettingFirstMutableMethodDeclaratively("#-1#") {
    returnType("V")
    accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
    parameterTypes("L")
}

internal val BytecodePatchContext.subtitleTrackMethod by gettingFirstMutableMethodDeclaratively("DISABLE_CAPTIONS_OPTION") {
    definingClass { endsWith("/SubtitleTrack;") }
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
}
