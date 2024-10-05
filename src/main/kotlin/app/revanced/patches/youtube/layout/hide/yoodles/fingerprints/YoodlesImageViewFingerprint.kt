package app.revanced.patches.youtube.layout.hide.yoodles.fingerprints

import app.revanced.patcher.extensions.or
import app.revanced.patches.youtube.layout.hide.yoodles.YoodlesResourcePatch
import app.revanced.util.patch.LiteralValueFingerprint
import com.android.tools.smali.dexlib2.AccessFlags

internal object YoodlesImageViewFingerprint : LiteralValueFingerprint(
    accessFlags = AccessFlags.PUBLIC or AccessFlags.FINAL,
    parameters = listOf("L", "L"),
    returnType = "Landroid/view/View;",
    literalSupplier = { YoodlesResourcePatch.youTubeLogo }
)