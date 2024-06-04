package app.revanced.patches.youtube.video.videoqualitymenu.fingerprints

import app.revanced.patches.youtube.video.videoqualitymenu.videoQualityQuickMenuAdvancedMenuDescription
import app.revanced.util.patch.literalValueFingerprint
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.Opcode

internal val videoQualityMenuOptionsFingerprint = literalValueFingerprint(
    literalSupplier = { videoQualityQuickMenuAdvancedMenuDescription },
) {
    accessFlags(AccessFlags.STATIC)
    returns("[L")
    parameters("Landroid/content/Context", "L", "L")
    opcodes(
        Opcode.IF_EQZ, // Check if advanced menu should be shown.
        Opcode.NEW_ARRAY,
        Opcode.APUT_OBJECT,
        Opcode.APUT_OBJECT,
        Opcode.APUT_OBJECT,
        Opcode.RETURN_OBJECT,
        Opcode.CONST_4, // Advanced menu code path.
    )
}
