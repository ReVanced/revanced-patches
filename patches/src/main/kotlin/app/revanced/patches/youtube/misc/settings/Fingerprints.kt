package app.revanced.patches.youtube.misc.settings

import app.revanced.patcher.InstructionLocation.MatchAfterWithin
import app.revanced.patcher.accessFlags
import app.revanced.patcher.gettingFirstMethodDeclaratively
import app.revanced.patcher.instructions
import app.revanced.patcher.invoke
import app.revanced.patcher.opcode
import app.revanced.patcher.parameterTypes
import app.revanced.patcher.patch.BytecodePatchContext
import app.revanced.patcher.returnType
import app.revanced.patches.shared.misc.mapping.ResourceType
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.Opcode

internal val BytecodePatchContext.licenseActivityOnCreateMethod by gettingFirstMethodDeclaratively {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
    returnType("V")
    parameterTypes("Landroid/os/Bundle;")
    custom { method, classDef ->
        method.name == "onCreate" && classDef.endsWith("/LicenseActivity;")
    }
}

internal val BytecodePatchContext.setThemeMethod by gettingFirstMethodDeclaratively {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
    returnType("L")
    parameterTypes()
    instructions(
        ResourceType.STRING("app_theme_appearance_dark"),
    )
}

internal val BytecodePatchContext.cairoFragmentConfigMethod by gettingFirstMethodDeclaratively {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
    returnType("Z")
    instructions(
        45532100L(),
        opcode(Opcode.MOVE_RESULT, location = MatchAfterWithin(10)),
    )
}

// Flag is present in 20.23, but bold icons are missing and forcing them crashes the app.
// 20.31 is the first target with all the bold icons present.
internal val BytecodePatchContext.boldIconsFeatureFlagMethod by gettingFirstMethodDeclaratively {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
    returnType("Z")
    parameterTypes()
    instructions(
        45685201L(),
    )
}
