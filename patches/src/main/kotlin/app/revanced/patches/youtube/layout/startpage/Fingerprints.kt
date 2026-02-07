package app.revanced.patches.youtube.layout.startpage

import app.revanced.patcher.*
import app.revanced.patcher.patch.BytecodePatchContext
import com.android.tools.smali.dexlib2.Opcode

internal val BytecodePatchContext.intentActionMethod by gettingFirstMethodDeclaratively(
    "has_handled_intent",
) {
    parameterTypes("Landroid/content/Intent;")
}

internal val BytecodePatchContext.browseIdMethodMatch by composingFirstMethod {
    returnType("L")
    // parameterTypes() // 20.30 and earlier is no parameters.  20.31+ parameter is L.
    instructions(
        "FEwhat_to_watch"(),
        512L(),
        allOf(Opcode.IPUT_OBJECT(), field { type == "Ljava/lang/String;" }),
    )
}
