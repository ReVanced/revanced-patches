package app.revanced.patches.reddit.customclients.sync.syncforreddit.fix.redgifs

import app.revanced.patcher.extensions.InstructionExtensions.instructions
import app.revanced.patcher.fingerprint
import app.revanced.util.indexOfFirstInstruction
import app.revanced.util.writeRegister
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.iface.instruction.formats.Instruction11n


internal val createOkHttpClientFingerprint = fingerprint {
    accessFlags(AccessFlags.PRIVATE, AccessFlags.STATIC)
    returns("V")
    parameters()
    custom { method, classDef ->
        // There are four functions (each creating a client) defined in this file with very similar fingerprints.
        // We're looking for the one that only creates one object (the builder) and sets client options true
        // (thus never reloading the register with a 0).
        classDef.sourceFile == "OkHttpHelper.java" &&
        method.instructions.count { it.opcode == Opcode.NEW_INSTANCE } == 1 &&
        method.indexOfFirstInstruction {
            opcode == Opcode.CONST_4 && writeRegister == 1 && (this as Instruction11n).narrowLiteral == 0
        } == -1
    }
}

internal val getDefaultUserAgentFingerprint = fingerprint {
    custom { method, classDef ->
        method.name == "getDefaultUserAgent" && classDef.type == EXTENSION_CLASS_DESCRIPTOR
    }
}

internal val getOriginalUserAgentFingerprint = fingerprint {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.STATIC)
    returns("Ljava/lang/String;")
    parameters()
    custom { _, classDef -> classDef.sourceFile == "AccountSingleton.java" }
}
