package app.revanced.patches.youtube.misc.autorepeat.fingerprints

import app.revanced.patcher.extensions.or
import app.revanced.patcher.fingerprint.method.impl.MethodFingerprint
import org.jf.dexlib2.AccessFlags

object AutoRepeatFingerprint : MethodFingerprint(
    "V",
    AccessFlags.PUBLIC or AccessFlags.FINAL,
    listOf(),
    customFingerprint = { methodDef -> methodDef.implementation!!.instructions.count() == 3 && methodDef.annotations.isEmpty()}
)