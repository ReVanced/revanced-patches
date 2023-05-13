package app.revanced.patches.spotify.audio.fingerprints


import app.revanced.patcher.extensions.or
import app.revanced.patcher.fingerprint.method.impl.MethodFingerprint
import org.jf.dexlib2.AccessFlags
import org.jf.dexlib2.Opcode
import org.jf.dexlib2.iface.instruction.ReferenceInstruction
import org.jf.dexlib2.iface.reference.MethodReference

object DisableCaptureRestrictionAudioDriverFingerprint : MethodFingerprint(
    "L",
    AccessFlags.PUBLIC or AccessFlags.STATIC or AccessFlags.SYNTHETIC or AccessFlags.BRIDGE,
    listOf("L"),
    listOf(
        Opcode.CONST_4,
        Opcode.INVOKE_VIRTUAL,
        Opcode.MOVE_RESULT_OBJECT,
        Opcode.RETURN_OBJECT
    ),
    customFingerprint = { methodDef, _ ->
        // Check for method call to AudioAttributes$Builder.setAllowedCapturePolicy Android API
        methodDef.implementation?.instructions?.any {
            ((it as? ReferenceInstruction)?.reference as? MethodReference)?.name == "setAllowedCapturePolicy"
        } == true
    }
)