package app.revanced.patches.youtube.layout.startpage

import app.revanced.patcher.*
import app.revanced.patcher.patch.BytecodePatchContext
import com.android.tools.smali.dexlib2.Opcode

internal val BytecodePatchContext.intentActionMethod by gettingFirstMutableMethodDeclaratively(
    "has_handled_intent",
) {
    parameterTypes("Landroid/content/Intent;")
}

internal val BytecodePatchContext.browseIdMethod by gettingFirstMutableMethodDeclaratively(
    "FEwhat_to_watch",
) {
    returnType("Lcom/google/android/apps/youtube/app/common/ui/navigation/PaneDescriptor;")
    // parameterTypes() // 20.30 and earlier is no parameters.  20.31+ parameter is L.
    instructions(
        512L(),
        allOf(Opcode.IPUT_OBJECT(), field { type == "Ljava/lang/String;" }),
    )
}
