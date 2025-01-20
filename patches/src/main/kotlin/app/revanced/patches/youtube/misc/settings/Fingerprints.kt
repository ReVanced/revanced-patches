package app.revanced.patches.youtube.misc.settings

import app.revanced.patcher.fingerprint
import app.revanced.patcher.literal
import app.revanced.patcher.opcode
import app.revanced.patches.shared.misc.mapping.resourceLiteral
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.Opcode

internal val licenseActivityOnCreateFingerprint by fingerprint {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
    returns("V")
    parameters("L")
    custom { method, classDef ->
        classDef.endsWith("LicenseActivity;") && method.name == "onCreate"
    }
}

internal val setThemeFingerprint by fingerprint {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
    returns("L")
    parameters()
    opcodes(Opcode.RETURN_OBJECT)
    literal { appearanceStringId }
}

/**
 * Added in YouTube v19.04.38.
 */
internal val cairoFragmentConfigFingerprint by fingerprint {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
    returns("Z")
    instructions(
        literal(45532100L),
        opcode(Opcode.MOVE_RESULT, 10)
    )
}