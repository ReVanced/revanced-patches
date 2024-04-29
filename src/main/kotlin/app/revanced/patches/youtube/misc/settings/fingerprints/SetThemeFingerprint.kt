package app.revanced.patches.youtube.misc.settings.fingerprints

import app.revanced.patches.youtube.misc.settings.appearanceStringId
import app.revanced.util.patch.literalValueFingerprint
import com.android.tools.smali.dexlib2.AccessFlags

val setThemeFingerprint = literalValueFingerprint(literalSupplier = { appearanceStringId }) {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
    returns("L")
    parameters()
    custom { methodDef, _ ->
        methodDef.definingClass.endsWith("ThemeHelper;") && methodDef.name == "setTheme"
    }
}
