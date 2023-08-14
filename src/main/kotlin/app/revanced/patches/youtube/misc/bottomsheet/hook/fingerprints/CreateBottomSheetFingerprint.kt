package app.revanced.patches.youtube.misc.bottomsheet.hook.fingerprints

import app.revanced.patcher.extensions.or
import app.revanced.patches.youtube.misc.bottomsheet.hook.patch.BottomSheetHookResourcePatch
import app.revanced.util.patch.LiteralValueFingerprint
import com.android.tools.smali.dexlib2.AccessFlags

object CreateBottomSheetFingerprint : LiteralValueFingerprint(
    accessFlags = AccessFlags.PUBLIC or AccessFlags.FINAL,
    parameters = listOf("L"),
    returnType = "Landroid/widget/LinearLayout;",
    literal = BottomSheetHookResourcePatch.bottomSheetMargins
)