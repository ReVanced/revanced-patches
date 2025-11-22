package app.revanced.patches.shared.layout.theme

import app.revanced.patcher.InstructionLocation.MatchAfterImmediately
import app.revanced.patcher.InstructionLocation.MatchAfterWithin
import app.revanced.patcher.fieldAccess
import app.revanced.patcher.fingerprint
import app.revanced.patcher.methodCall
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.Opcode

internal val lithoOnBoundsChangeFingerprint = fingerprint {
    accessFlags(AccessFlags.PROTECTED, AccessFlags.FINAL)
    returns("V")
    parameters("Landroid/graphics/Rect;")
    instructions(
        fieldAccess(
            opcode = Opcode.IPUT_OBJECT,
            definingClass = "this",
            type = "Landroid/graphics/Path;"
        ),

        methodCall(
            definingClass = "this",
            name = "isStateful",
            returnType = "Z",
            location = MatchAfterWithin(5)
        ),

        fieldAccess(
            opcode = Opcode.IGET_OBJECT,
            definingClass = "this",
            type = "Landroid/graphics/Paint",
            location = MatchAfterWithin(5)
        ),
        methodCall(
            smali = "Landroid/graphics/Paint;->setColor(I)V",
            location = MatchAfterImmediately()
        )
    )
    custom { method, _ ->
        method.name == "onBoundsChange"
    }
}
