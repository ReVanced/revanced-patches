package app.revanced.patches.youtube.general.channelprofile.fingerprints

import app.revanced.patches.youtube.utils.resourceid.SharedResourceIdPatch.TabsBarTextTabView
import app.revanced.util.fingerprint.LiteralValueFingerprint

object DefaultsTabsBarFingerprint : LiteralValueFingerprint(
    returnType = "Landroid/view/View;",
    parameters = listOf("Ljava/lang/CharSequence;", "Ljava/lang/CharSequence;", "Z"),
    literalSupplier = { TabsBarTextTabView }
)