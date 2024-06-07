package app.revanced.patches.youtube.video.videoqualitymenu.fingerprints

import app.revanced.patches.youtube.video.videoqualitymenu.RestoreOldVideoQualityMenuResourcePatch
import app.revanced.util.patch.LiteralValueFingerprint
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.Opcode

internal object VideoQualityMenuOptionsFingerprint : LiteralValueFingerprint(
    accessFlags = AccessFlags.STATIC.value,
    parameters = listOf("Landroid/content/Context", "L", "L"),
    returnType = "[L",
    opcodes = listOf(
        Opcode.CONST_4, // First instruction of method.
        Opcode.CONST_4,
        Opcode.IF_EQZ,
        Opcode.IGET_BOOLEAN, // Use the quality menu, that contains the advanced menu.
        Opcode.IF_NEZ
    ),
    literalSupplier = { RestoreOldVideoQualityMenuResourcePatch.videoQualityQuickMenuAdvancedMenuDescription }
)