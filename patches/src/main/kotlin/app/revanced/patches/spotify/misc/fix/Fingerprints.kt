package app.revanced.patches.spotify.misc.fix

import app.revanced.patcher.fingerprint
import app.revanced.patcher.methodCall
import app.revanced.patcher.opcode
import app.revanced.patcher.string
import com.android.tools.smali.dexlib2.Opcode

internal val getAppSignatureFingerprint by fingerprint {
    instructions(
        methodCall(parameters = listOf("Ljava/util/Collection;"), returnType = "Ljava/lang/String;"),
        opcode(Opcode.MOVE_RESULT_OBJECT, maxAfter = 0),
        string("Failed to get the application signatures", maxAfter = 10)
    )
}
