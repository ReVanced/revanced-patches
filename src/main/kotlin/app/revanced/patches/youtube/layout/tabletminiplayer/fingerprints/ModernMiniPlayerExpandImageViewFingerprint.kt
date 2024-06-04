package app.revanced.patches.youtube.layout.tabletminiplayer.fingerprints

import app.revanced.patcher.extensions.or
import app.revanced.patches.youtube.layout.tabletminiplayer.TabletMiniPlayerResourcePatch
import app.revanced.util.patch.LiteralValueFingerprint
import com.android.tools.smali.dexlib2.AccessFlags

internal object ModernMiniPlayerExpandImageViewFingerprint : LiteralValueFingerprint(
    accessFlags = AccessFlags.PUBLIC or AccessFlags.FINAL,
    returnType = "Landroid/widget/ImageView;",
    parameters = listOf(),
    literalSupplier = { TabletMiniPlayerResourcePatch.modernMiniplayerExpand }
)