package app.revanced.patches.youtube.layout.autocaptions

import app.revanced.patcher.*
import app.revanced.patcher.patch.BytecodePatchContext
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.Opcode

internal val BytecodePatchContext.startVideoInformerMethod by gettingFirstMethodDeclaratively("pc") {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
    returnType("V")
    opcodes(
        Opcode.INVOKE_INTERFACE,
        Opcode.RETURN_VOID,
    )
}

internal val BytecodePatchContext.subtitleTrackMethod by gettingFirstMethodDeclaratively("DISABLE_CAPTIONS_OPTION") {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
    returnType("Z")
    parameterTypes()
}
