package app.revanced.patches.reddit.ad.general

import app.revanced.patcher.definingClass
import app.revanced.patcher.gettingFirstMethodDeclaratively
import app.revanced.patcher.instructions
import app.revanced.patcher.invoke
import app.revanced.patcher.patch.BytecodePatchContext
import app.revanced.patcher.returnType
import com.android.tools.smali.dexlib2.Opcode

internal val BytecodePatchContext.adPostMethod by gettingFirstMethodDeclaratively("children") {
    definingClass("Listing;")
    returnType("V")
}

internal val BytecodePatchContext.newAdPostMethod by gettingFirstMethodDeclaratively(
    "feedElement",
    "com.reddit.cookie",
) {
    instructions(Opcode.INVOKE_VIRTUAL())
}
