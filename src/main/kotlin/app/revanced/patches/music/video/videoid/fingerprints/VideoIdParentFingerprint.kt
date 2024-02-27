package app.revanced.patches.music.video.videoid.fingerprints

import app.revanced.patcher.extensions.or
import app.revanced.patcher.fingerprint.MethodFingerprint
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.Opcode

object VideoIdParentFingerprint : MethodFingerprint(
    returnType = "V",
    parameters = emptyList(),
    accessFlags = AccessFlags.PUBLIC or AccessFlags.FINAL,
    opcodes = listOf(
        Opcode.INVOKE_SUPER,
        Opcode.IGET_OBJECT,
        Opcode.CONST_4,
        Opcode.IPUT_OBJECT,
        Opcode.IGET_OBJECT
    ),
    customFingerprint = { methodDef, _ -> methodDef.definingClass.endsWith("/WatchFragment;") && methodDef.name == "onDestroyView" }
)
