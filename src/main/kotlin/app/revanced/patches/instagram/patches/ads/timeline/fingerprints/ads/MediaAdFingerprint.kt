package app.revanced.patches.instagram.patches.ads.timeline.fingerprints.ads

import app.revanced.patcher.extensions.or
import app.revanced.patcher.fingerprint.method.impl.MethodFingerprint
import org.jf.dexlib2.AccessFlags
import org.jf.dexlib2.Opcode
import org.jf.dexlib2.iface.Method

abstract class MediaAdFingerprint(
    returnType: String? = "Z",
    access: Int? = AccessFlags.PUBLIC or AccessFlags.FINAL,
    parameters: Iterable<String>? = listOf(),
    opcodes: Iterable<Opcode>?,
    customFingerprint: ((methodDef: Method) -> Boolean)? = null
) : MethodFingerprint(
    returnType,
    access,
    parameters,
    opcodes,
    customFingerprint = customFingerprint
) {
    abstract override fun toString(): String
}