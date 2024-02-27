package app.revanced.patches.music.account.component.fingerprints

import app.revanced.patches.music.utils.resourceid.SharedResourceIdPatch.MenuEntry
import app.revanced.util.fingerprint.LiteralValueFingerprint

object MenuEntryFingerprint : LiteralValueFingerprint(
    returnType = "V",
    literalSupplier = { MenuEntry }
)
