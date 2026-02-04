package app.revanced.patches.soundcloud.shared

import app.revanced.patcher.accessFlags
import app.revanced.patcher.gettingFirstMethodDeclaratively
import app.revanced.patcher.opcodes
import app.revanced.patcher.parameterTypes
import app.revanced.patcher.patch.BytecodePatchContext
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.Opcode

internal val BytecodePatchContext.featureConstructorMethod by gettingFirstMethodDeclaratively {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.CONSTRUCTOR)
    parameterTypes("Ljava/lang/String;", "Z", "Ljava/util/List;")
    opcodes(
        Opcode.SGET_OBJECT,
        Opcode.CHECK_CAST,
        Opcode.INVOKE_VIRTUAL
    )
}
