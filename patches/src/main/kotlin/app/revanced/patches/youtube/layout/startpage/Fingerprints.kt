package app.revanced.patches.youtube.layout.startpage

import app.revanced.patcher.addString
import app.revanced.patcher.fieldAccess
import app.revanced.patcher.gettingFirstMethodDeclaratively
import app.revanced.patcher.instructions
import app.revanced.patcher.invoke
import app.revanced.patcher.parameterTypes
import app.revanced.patcher.patch.BytecodePatchContext
import app.revanced.patcher.returnType
import com.android.tools.smali.dexlib2.Opcode

internal val BytecodePatchContext.intentActionMethod by gettingFirstMethodDeclaratively {
    parameterTypes("Landroid/content/Intent;")
    instructions(
        "has_handled_intent"(),
    )
}

internal val BytecodePatchContext.browseIdMethod by gettingFirstMethodDeclaratively {
    returnType("Lcom/google/android/apps/youtube/app/common/ui/navigation/PaneDescriptor;")

    // parameterTypes() // 20.30 and earlier is no parameters.  20.31+ parameter is L.
    instructions(
        "FEwhat_to_watch"(),
        512(),
        fieldAccess(opcode = Opcode.IPUT_OBJECT, type = "Ljava/lang/String;"),
    )
}
