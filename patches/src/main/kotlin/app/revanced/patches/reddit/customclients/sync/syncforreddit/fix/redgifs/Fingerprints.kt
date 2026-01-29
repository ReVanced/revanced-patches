package app.revanced.patches.reddit.customclients.sync.syncforreddit.fix.redgifs

import app.revanced.patcher.*
import app.revanced.patcher.extensions.instructions
import app.revanced.patcher.gettingFirstMutableMethodDeclaratively
import app.revanced.patcher.patch.BytecodePatchContext
import app.revanced.util.indexOfFirstInstruction
import app.revanced.util.writeRegister
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.iface.instruction.NarrowLiteralInstruction

internal val BytecodePatchContext.createOkHttpClientMethod by gettingFirstMutableMethodDeclaratively {
    accessFlags(AccessFlags.PRIVATE, AccessFlags.STATIC)
    returnType("V")
    parameterTypes()
    custom {
        // There are four functions (each creating a client) defined in this file with very similar methods.
        // We're looking for the one that only creates one object (the builder) and sets client options true
        // (thus never reloading the register with a 0).
        immutableClassDef.sourceFile == "OkHttpHelper.java" && instructions.count {
            it.opcode == Opcode.NEW_INSTANCE
        } == 1 && indexOfFirstInstruction {
            opcode == Opcode.CONST_4 && writeRegister == 1 && (this as NarrowLiteralInstruction).narrowLiteral == 0
        } == -1
    }
}

internal val BytecodePatchContext.getDefaultUserAgentMethod by gettingFirstMutableMethodDeclaratively {
    name("getDefaultUserAgent")
    definingClass(EXTENSION_CLASS_DESCRIPTOR)
}

internal val BytecodePatchContext.getOriginalUserAgentMethod by gettingFirstMutableMethodDeclaratively {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.STATIC)
    returnType { startsWith("Ljava/lang/String;") }
    parameterTypes()
    custom { immutableClassDef.sourceFile == "AccountSingleton.java" }
}
