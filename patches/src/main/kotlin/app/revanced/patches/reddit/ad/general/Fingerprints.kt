package app.revanced.patches.nunl.ads

import app.revanced.patcher.BytecodePatchContextMethodMatching.gettingFirstMutableMethodDeclaratively
import app.revanced.patcher.definingClass
import app.revanced.patcher.instructions
import app.revanced.patcher.invoke
import app.revanced.patcher.patch.BytecodePatchContext
import app.revanced.patcher.returnType
import com.android.tools.smali.dexlib2.Opcode

internal val BytecodePatchContext.adPostMethod by gettingFirstMutableMethodDeclaratively("children") {
    returnType("V")
    // "children" are present throughout multiple versions
    instructions("children"())
    definingClass { endsWith("Listing;") }
}

internal val BytecodePatchContext.newAdPostMethod by gettingFirstMutableMethodDeclaratively(
    "feedElement", "com.reddit.cookie"
) {
    instructions(Opcode.INVOKE_VIRTUAL())
    instructions("feedElement"())
    instructions("com.reddit.cookie"())
}
