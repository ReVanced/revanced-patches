package app.revanced.patches.youtube.video.quality

import app.revanced.patcher.fingerprint
import app.revanced.patcher.string
import app.revanced.util.literal
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.Opcode

internal const val YOUTUBE_VIDEO_QUALITY_CLASS_TYPE = "Lcom/google/android/libraries/youtube/innertube/model/media/VideoQuality;"

/**
 * YouTube 20.19 and lower.
 */
internal val videoQualityLegacyFingerprint by fingerprint {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.CONSTRUCTOR)
    parameters(
        "I", // Resolution.
        "Ljava/lang/String;", // Human readable resolution: "480p", "1080p Premium", etc
        "Z",
        "L"
    )
    custom { _, classDef ->
        classDef.type == YOUTUBE_VIDEO_QUALITY_CLASS_TYPE
    }
}

internal val videoQualityFingerprint by fingerprint {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.CONSTRUCTOR)
    parameters(
        "I", // Resolution.
        "L",
        "Ljava/lang/String;", // Human readable resolution: "480p", "1080p Premium", etc
        "Z",
        "L"
    )
    custom { _, classDef ->
        classDef.type == YOUTUBE_VIDEO_QUALITY_CLASS_TYPE
    }
}

/**
 * Matches with the class found in [videoQualitySetterFingerprint].
 */
internal val setVideoQualityFingerprint by fingerprint {
    returns("V")
    parameters("L")
    opcodes(
        Opcode.IGET_OBJECT,
        Opcode.IPUT_OBJECT,
        Opcode.IGET_OBJECT,
    )
}

internal val videoQualityItemOnClickParentFingerprint by fingerprint {
    returns("V")
    instructions(
        string("VIDEO_QUALITIES_MENU_BOTTOM_SHEET_FRAGMENT"),
    )
}

/**
 * Resolves to class found in [videoQualityItemOnClickFingerprint].
 */
internal val videoQualityItemOnClickFingerprint by fingerprint {
    returns("V")
    parameters(
        "Landroid/widget/AdapterView;",
        "Landroid/view/View;",
        "I",
        "J"
    )
    custom { method, _ ->
        method.name == "onItemClick"
    }
}

internal val videoQualitySetterFingerprint by fingerprint {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
    returns("V")
    parameters("[L", "I", "Z")
    opcodes(
        Opcode.IF_EQZ,
        Opcode.INVOKE_VIRTUAL,
        Opcode.MOVE_RESULT_OBJECT,
        Opcode.INVOKE_VIRTUAL,
        Opcode.IPUT_BOOLEAN,
    )
    strings("menu_item_video_quality")
}

internal val videoQualityMenuOptionsFingerprint by fingerprint {
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
    literal { videoQualityQuickMenuAdvancedMenuDescription }
}

internal val videoQualityMenuViewInflateFingerprint by fingerprint {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
    returns("L")
    parameters("L", "L", "L")
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
