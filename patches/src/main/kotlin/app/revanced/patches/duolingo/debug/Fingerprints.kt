package app.revanced.patches.duolingo.debug

import app.revanced.patcher.fingerprint
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.Opcode

internal val debugCategoryAllowOnReleaseBuildsFingerprint = fingerprint {
    returns("Z")
    parameters()
    custom { method, classDef ->
        method.name == "getAllowOnReleaseBuilds" && classDef.type == "Lcom/duolingo/debug/DebugCategory;"
    }
}

internal val buildConfigProviderConstructorFingerprint = fingerprint {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.CONSTRUCTOR)
    parameters()
    opcodes(Opcode.CONST_4)
}

internal val buildConfigProviderToStringFingerprint = fingerprint {
    parameters()
    returns("Ljava/lang/String;")
    strings("BuildConfigProvider(") // Partial string match.
    custom { method, _ ->
        method.name == "toString"
    }
}
