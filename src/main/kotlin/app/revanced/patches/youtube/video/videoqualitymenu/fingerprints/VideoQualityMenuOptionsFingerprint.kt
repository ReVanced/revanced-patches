package app.revanced.patches.youtube.video.videoqualitymenu.fingerprints

import app.revanced.patcher.fingerprint.methodFingerprint
import app.revanced.patches.youtube.video.videoqualitymenu.videoQualityQuickMenuAdvancedMenuDescription
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.Opcode

internal val videoQualityMenuOptionsFingerprint = methodFingerprint(
    literal { videoQualityQuickMenuAdvancedMenuDescription },
) {
    accessFlags(AccessFlags.STATIC)
    returns("[L")
    parameters("Landroid/content/Context", "L", "L")
    opcodes(
        Opcode.CONST_4, // First instruction of method.
        Opcode.CONST_4,
        Opcode.IF_EQZ,
        Opcode.IGET_BOOLEAN, // Use the quality menu, that contains the advanced menu.
        Opcode.IF_NEZ,
    )
}
