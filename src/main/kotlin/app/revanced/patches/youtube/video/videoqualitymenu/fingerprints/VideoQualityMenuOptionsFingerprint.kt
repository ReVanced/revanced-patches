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
        Opcode.IF_EQZ, // Check if advanced menu should be shown.
        Opcode.NEW_ARRAY,
        Opcode.APUT_OBJECT,
        Opcode.APUT_OBJECT,
        Opcode.APUT_OBJECT,
        Opcode.RETURN_OBJECT,
        Opcode.CONST_4 // Advanced menu code path.
    ),
    literalSupplier = { RestoreOldVideoQualityMenuResourcePatch.videoQualityQuickMenuAdvancedMenuDescription }
)