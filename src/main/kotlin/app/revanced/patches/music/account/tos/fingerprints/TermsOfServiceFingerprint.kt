package app.revanced.patches.music.account.tos.fingerprints

import app.revanced.patches.music.utils.resourceid.SharedResourceIdPatch.TosFooter
import app.revanced.util.fingerprint.LiteralValueFingerprint

object TermsOfServiceFingerprint : LiteralValueFingerprint(
    returnType = "Landroid/view/View;",
    literalSupplier = { TosFooter }
)
