package app.revanced.patches.messenger.layout

import app.revanced.patcher.*
import app.revanced.patcher.patch.BytecodePatchContext
import com.android.tools.smali.dexlib2.Opcode

internal val BytecodePatchContext.isFacebookButtonEnabledMethod by gettingFirstMethodDeclaratively {
    parameterTypes()
    returnType("Z")
    instructions("FacebookButtonTabButtonImplementation"(String::contains))
}

internal val BytecodePatchContext.renderRedesignedDrawerMethodMatch by composingFirstMethod("Cannot render redesigned drawer with search icon ") {
    instructions(
        allOf(
            Opcode.INVOKE_VIRTUAL(),
            method { returnType == "Z" && parameterTypes.isEmpty() }
        )
    )
}