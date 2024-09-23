package app.revanced.patches.youtube.misc.playercontrols.fingerprints

import app.revanced.patcher.extensions.or
import app.revanced.patcher.fingerprint.MethodFingerprint
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.Opcode

/**
 * Resolves to the class found in [MotionOverlayFingerprint].
 */
internal object MotionOverlayVisibilityFingerprint : MethodFingerprint(
    accessFlags = AccessFlags.PUBLIC or AccessFlags.FINAL,
    returnType = "V",
    parameters = listOf("Z"),
    opcodes = listOf(
        Opcode.IPUT_BOOLEAN,
        Opcode.IGET_BOOLEAN,
        Opcode.IF_EQZ,
        Opcode.IGET_OBJECT,
        Opcode.IPUT_BOOLEAN,
        Opcode.RETURN_VOID,
//iput-boolean p1, p0, Lafxx;->j:Z
//iget-boolean v0, p0, Lafxx;->o:Z
//if-eqz v0, :cond_a
//iget-object v0, p0, Lafxx;->i:Lafxr;
//iput-boolean p1, v0, Lafxr;->f:Z
//return-void
        )
)