package app.revanced.patches.youtube.misc.autorepeat.fingerprints

import app.revanced.patcher.annotation.Name
import app.revanced.patcher.annotation.Version
import app.revanced.patcher.extensions.or
import app.revanced.patcher.fingerprint.method.impl.MethodFingerprint
import app.revanced.patches.youtube.misc.autorepeat.annotations.AutoRepeatCompatibility
import org.jf.dexlib2.AccessFlags

@Name("auto-repeat-fingerprint")
@AutoRepeatCompatibility
@Version("0.0.1")
object AutoRepeatFingerprint : MethodFingerprint(
    "V",
    AccessFlags.PUBLIC or AccessFlags.FINAL,
    listOf(),
    customFingerprint = { methodDef -> methodDef.implementation!!.instructions.count() == 3 && methodDef.annotations.isEmpty()}
)