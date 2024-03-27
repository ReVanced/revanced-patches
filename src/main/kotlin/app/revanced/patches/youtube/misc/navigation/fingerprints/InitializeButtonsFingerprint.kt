package app.revanced.patches.youtube.misc.navigation.fingerprints

import app.revanced.patcher.extensions.or
import app.revanced.patches.youtube.misc.navigation.NavigationBarHookResourcePatch
import app.revanced.util.patch.LiteralValueFingerprint
import com.android.tools.smali.dexlib2.AccessFlags

/**
 * Resolves to the class found in [PivotBarConstructorFingerprint].
 */
internal object InitializeButtonsFingerprint : LiteralValueFingerprint(
    accessFlags = AccessFlags.PUBLIC or AccessFlags.FINAL,
    returnType = "V",
    parameters = listOf(),
    literalSupplier = { NavigationBarHookResourcePatch.imageOnlyTabResourceId }
)