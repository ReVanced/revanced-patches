package app.revanced.patches.finanzonline.detection.root.fingerprints

import app.revanced.patcher.fingerprint.methodFingerprint
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.Opcode

// Located @ at.gv.bmf.bmf2go.taxequalization.tools.utils.RootDetection#isRooted (3.0.1)
internal val rootDetectionFingerprint = methodFingerprint {
    accessFlags(AccessFlags.PUBLIC,AccessFlags.STATIC)
    returns("L")
    parameters("L")
    opcodes(
        Opcode.NEW_INSTANCE,
        Opcode.INVOKE_DIRECT,
        Opcode.INVOKE_VIRTUAL,
        Opcode.MOVE_RESULT,
        Opcode.INVOKE_STATIC,
        Opcode.MOVE_RESULT_OBJECT,
        Opcode.RETURN_OBJECT
    )
}
