package app.revanced.patches.reddit.customclients.sync.detection.piracy

import app.revanced.patcher.gettingFirstMutableMethodDeclarativelyOrNull
import app.revanced.patcher.invoke
import app.revanced.patcher.accessFlags
import app.revanced.patcher.custom
import app.revanced.patcher.extensions.instructions
import app.revanced.patcher.extensions.reference
import app.revanced.patcher.instructions
import app.revanced.patcher.patch.BytecodePatchContext
import app.revanced.patcher.returnType
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.Opcode

internal val BytecodePatchContext.detectPiracyMethodOrNull by gettingFirstMutableMethodDeclarativelyOrNull(
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
    // TODO: Convert to instructions() extension.
    custom {
        instructions.any {
            it.reference.toString() == "Lcom/github/javiersantos/piracychecker/PiracyChecker;"
        }
    }
}