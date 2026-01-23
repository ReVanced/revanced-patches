package app.revanced.patches.duolingo.debug

import app.revanced.patcher.*
import app.revanced.patcher.patch.BytecodePatchContext
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.Opcode

internal val BytecodePatchContext.debugCategoryAllowOnReleaseBuildsMethod by gettingFirstMutableMethodDeclaratively {
    name("getAllowOnReleaseBuilds")
    definingClass("Lcom/duolingo/debug/DebugCategory;")
    returnType("Z")
    parameterTypes()
}

internal val buildConfigProviderConstructorMethodMatch = firstMethodComposite {

    accessFlags(AccessFlags.PUBLIC, AccessFlags.CONSTRUCTOR)
    parameterTypes()
    opcodes(Opcode.CONST_4)
}

internal val BytecodePatchContext.buildConfigProviderToStringMethod by gettingFirstMutableMethodDeclaratively {
    name("toString")
    parameterTypes()
    returnType("Ljava/lang/String;")
    instructions(string("BuildConfigProvider(", String::contains))
}
