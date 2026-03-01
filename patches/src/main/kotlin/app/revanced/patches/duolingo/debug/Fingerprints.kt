package app.revanced.patches.duolingo.debug

import app.revanced.patcher.*
import app.revanced.patcher.patch.BytecodePatchContext
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.iface.ClassDef

internal val BytecodePatchContext.debugCategoryAllowOnReleaseBuildsMethod by gettingFirstMethodDeclaratively {
    name("getAllowOnReleaseBuilds")
    definingClass("Lcom/duolingo/debug/DebugCategory;")
    returnType("Z")
    parameterTypes()
}

internal val ClassDef.buildConfigProviderConstructorMethodMatch by ClassDefComposing.composingFirstMethod {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.CONSTRUCTOR)
    parameterTypes()
    opcodes(Opcode.CONST_4)
}

internal val BytecodePatchContext.buildConfigProviderToStringMethod by gettingFirstMethodDeclaratively {
    name("toString")
    parameterTypes()
    returnType("Ljava/lang/String;")
    instructions(string("BuildConfigProvider(", String::contains))
}
