package app.revanced.patches.youtube.general.accountmenu.fingerprints

import app.revanced.patches.youtube.utils.resourceid.SharedResourceIdPatch.CompactListItem
import app.revanced.util.fingerprint.LiteralValueFingerprint

object AccountListParentFingerprint : LiteralValueFingerprint(
    literalSupplier = { CompactListItem }
)