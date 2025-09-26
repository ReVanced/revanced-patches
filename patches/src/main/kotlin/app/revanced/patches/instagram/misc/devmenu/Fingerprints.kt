
package app.revanced.patches.instagram.misc.devmenu

import app.revanced.patcher.extensions.InstructionExtensions.instructions
import app.revanced.patcher.fingerprint
import app.revanced.util.literal
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.Opcode

internal val devMenuFingerprint = fingerprint {
    parameters("Lcom/instagram/common/session/UserSession;")
    returns("Z")
    accessFlags(AccessFlags.PUBLIC, AccessFlags.STATIC, AccessFlags.FINAL)
    literal { 36328139059713258L }
}
