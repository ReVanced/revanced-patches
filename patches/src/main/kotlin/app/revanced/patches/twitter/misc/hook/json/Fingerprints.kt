package app.revanced.patches.twitter.misc.hook.json

import app.revanced.patcher.*
import app.revanced.patcher.BytecodePatchContextClassDefMatching.gettingFirstClassDef
import app.revanced.patcher.ClassDefMethodMatching.firstMutableMethodDeclaratively
import app.revanced.patcher.patch.BytecodePatchContext
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.iface.ClassDef

internal val jsonHookPatchMethodMatch = firstMethodComposite {
    name("<clinit>")
    instructions(
        Opcode.INVOKE_INTERFACE(), // Add dummy hook to hooks list.
        // Add hooks to the hooks list.
        Opcode.INVOKE_STATIC(), // Call buildList.
    )
}

context(_: BytecodePatchContext)
internal fun ClassDef.getJsonInputStreamMethod() = firstMutableMethodDeclaratively {
    custom {
        if (parameterTypes.isEmpty()) false
        else parameterTypes.first() == "Ljava/io/InputStream;"
    }
}

internal val BytecodePatchContext.loganSquareClassDef by gettingFirstClassDef {
    type.endsWith("LoganSquare;")
}
