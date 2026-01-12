package app.revanced.patches.reddit.customclients.sync.detection.piracy

import app.revanced.patcher.BytecodePatchContextMethodMatching.gettingFirstMutableMethodDeclaratively
import app.revanced.patcher.accessFlags
import app.revanced.patcher.instructions
import app.revanced.patcher.invoke
import app.revanced.patcher.patch.BytecodePatchContext
import app.revanced.patcher.returnType
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.Opcode

internal val BytecodePatchContext.piracyDetectionMethod by gettingFirstMutableMethodDeclaratively(
    "Lcom/github/javiersantos/piracychecker/PiracyChecker;"
) {
    accessFlags(AccessFlags.PRIVATE, AccessFlags.FINAL)
    returnType("V")
    instructions(
        Opcode.NEW_INSTANCE(),
        Opcode.INVOKE_DIRECT(),
        Opcode.NEW_INSTANCE(),
        Opcode.INVOKE_DIRECT(),
        Opcode.INVOKE_VIRTUAL(),
    )
}
