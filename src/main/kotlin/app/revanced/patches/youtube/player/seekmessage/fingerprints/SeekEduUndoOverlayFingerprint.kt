package app.revanced.patches.youtube.player.seekmessage.fingerprints

import app.revanced.patches.youtube.utils.resourceid.SharedResourceIdPatch.SeekUndoEduOverlayStub
import app.revanced.util.fingerprint.LiteralValueFingerprint

object SeekEduUndoOverlayFingerprint : LiteralValueFingerprint(
    returnType = "V",
    literalSupplier = { SeekUndoEduOverlayStub }
)