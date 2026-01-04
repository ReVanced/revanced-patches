package app.revanced.patches.youtube.misc.settings

import app.revanced.patcher.InstructionLocation.MatchAfterWithin
import app.revanced.patcher.fingerprint
import app.revanced.patcher.literal
import app.revanced.patcher.opcode
import app.revanced.patches.shared.misc.mapping.ResourceType
import app.revanced.patches.shared.misc.mapping.resourceLiteral
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.Opcode

internal val licenseActivityOnCreateFingerprint = fingerprint {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
    returns("V")
    parameters("Landroid/os/Bundle;")
    custom { method, classDef ->
        method.name == "onCreate" && classDef.endsWith("/LicenseActivity;")
    }
}

internal val setThemeFingerprint = fingerprint {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
    returns("L")
    parameters()
    instructions(
        resourceLiteral(ResourceType.STRING, "app_theme_appearance_dark"),
    )
}

internal val cairoFragmentConfigFingerprint = fingerprint {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
    returns("Z")
    instructions(
        literal(45532100L),
        opcode(Opcode.MOVE_RESULT, location = MatchAfterWithin(10))
    )
}

// Flag is present in 20.23, but bold icons are missing and forcing them crashes the app.
// 20.31 is the first target with all the bold icons present.
internal val boldIconsFeatureFlagFingerprint = fingerprint {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
    returns("Z")
    parameters()
    instructions(
        literal(45685201L)
    )
}
