package app.revanced.patches.youtube.misc.litho.lazily

import app.revanced.patcher.accessFlags
import app.revanced.patcher.allOf
import app.revanced.patcher.definingClass
import app.revanced.patcher.firstMethodDeclaratively
import app.revanced.patcher.gettingFirstMethodDeclaratively
import app.revanced.patcher.instructions
import app.revanced.patcher.invoke
import app.revanced.patcher.method
import app.revanced.patcher.name
import app.revanced.patcher.patch.BytecodePatchContext
import app.revanced.patcher.returnType
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.iface.ClassDef

internal val BytecodePatchContext.componentContextParserMethod by gettingFirstMethodDeclaratively {
    returnType("L")
    instructions(
        "Failed to parse Element proto."(),
        "Cannot read theme key from model."()
    )
}

context(_: BytecodePatchContext)
internal fun ClassDef.getTreeNodeResultListMethod() = firstMethodDeclaratively {
    accessFlags(AccessFlags.PRIVATE, AccessFlags.FINAL)
    returnType("Ljava/util/List;")
    instructions(
        allOf(Opcode.INVOKE_STATIC(), method { name == "nCopies" })
    )
}

internal val BytecodePatchContext.lazilyConvertedElementPatchMethod by gettingFirstMethodDeclaratively {
    name("onLazilyConvertedElementLoaded")
    definingClass(EXTENSION_CLASS_DESCRIPTOR)
    accessFlags(AccessFlags.PRIVATE, AccessFlags.STATIC)
}
