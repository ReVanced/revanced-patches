package app.revanced.patches.youtube.layout.hide.general.fingerprints

import app.revanced.patcher.extensions.or
import app.revanced.patches.youtube.layout.hide.general.HideLayoutComponentsResourcePatch
import app.revanced.util.patch.LiteralValueFingerprint
import com.android.tools.smali.dexlib2.AccessFlags

internal object YoodlesImageViewFingerprint : LiteralValueFingerprint(
    accessFlags = AccessFlags.PUBLIC or AccessFlags.FINAL,
    parameters = listOf("L", "L"),
    returnType = "Landroid/view/View;",
    literalSupplier = { HideLayoutComponentsResourcePatch.youTubeLogo }
)