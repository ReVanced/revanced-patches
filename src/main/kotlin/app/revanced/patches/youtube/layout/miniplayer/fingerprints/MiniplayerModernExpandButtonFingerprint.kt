package app.revanced.patches.youtube.layout.miniplayer.fingerprints

import app.revanced.patcher.extensions.or
import app.revanced.patches.youtube.layout.miniplayer.MiniplayerResourcePatch
import app.revanced.util.patch.LiteralValueFingerprint
import com.android.tools.smali.dexlib2.AccessFlags

/**
 * Resolves using the class found in [MiniplayerModernViewParentFingerprint].
 */
internal object MiniplayerModernExpandButtonFingerprint : LiteralValueFingerprint(
    accessFlags = AccessFlags.PUBLIC or AccessFlags.FINAL,
    returnType = "Landroid/widget/ImageView;",
    parameters = listOf(),
    literalSupplier = { MiniplayerResourcePatch.modernMiniplayerExpand }
)