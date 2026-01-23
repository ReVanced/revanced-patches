package app.revanced.patches.youtube.video.quality

import app.revanced.patcher.addString
import app.revanced.patcher.fingerprint
import app.revanced.util.literal
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.Opcode

internal val videoQualityItemOnClickParentFingerprint = fingerprint {
    returnType("V")
    instructions(
        addString("VIDEO_QUALITIES_MENU_BOTTOM_SHEET_FRAGMENT"),
    )
}

/**
 * Resolves to class found in [videoQualityItemOnClickFingerprint].
 */
internal val videoQualityItemOnClickFingerprint = fingerprint {
    returnType("V")
    parameterTypes(
        "Landroid/widget/AdapterView;",
        "Landroid/view/View;",
        "I",
        "J",
    )
    custom { method, _ ->
        method.name == "onItemClick"
    }
}

internal val videoQualityMenuOptionsFingerprint = fingerprint {
    accessFlags(AccessFlags.STATIC)
    returnType("[L")
    parameterTypes("Landroid/content/Context", "L", "L")
    opcodes(
        Opcode.CONST_4, // First instruction of method.
        Opcode.CONST_4,
        Opcode.IF_EQZ,
        Opcode.IGET_BOOLEAN, // Use the quality menu, that contains the advanced menu.
        Opcode.IF_NEZ,
    )
    literal { videoQualityQuickMenuAdvancedMenuDescription }
}

internal val videoQualityMenuViewInflateFingerprint = fingerprint {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
    returnType("L")
    parameterTypes("L", "L", "L")
    opcodes(
        Opcode.INVOKE_SUPER,
        Opcode.CONST,
        Opcode.CONST_4,
        Opcode.INVOKE_VIRTUAL,
        Opcode.MOVE_RESULT_OBJECT,
        Opcode.CONST,
        Opcode.INVOKE_VIRTUAL,
        Opcode.MOVE_RESULT_OBJECT,
        Opcode.CONST_16,
        Opcode.INVOKE_VIRTUAL,
        Opcode.CONST,
        Opcode.INVOKE_VIRTUAL,
        Opcode.MOVE_RESULT_OBJECT,
        Opcode.CHECK_CAST,
    )
    literal { videoQualityBottomSheetListFragmentTitle }
}
