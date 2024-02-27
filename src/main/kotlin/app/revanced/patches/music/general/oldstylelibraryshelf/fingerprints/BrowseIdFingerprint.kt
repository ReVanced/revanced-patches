package app.revanced.patches.music.general.oldstylelibraryshelf.fingerprints

import app.revanced.patcher.extensions.or
import app.revanced.util.fingerprint.LiteralValueFingerprint
import com.android.tools.smali.dexlib2.AccessFlags

object BrowseIdFingerprint : LiteralValueFingerprint(
    returnType = "L",
    accessFlags = AccessFlags.PUBLIC or AccessFlags.FINAL,
    parameters = listOf("L"),
    strings = listOf("FEmusic_offline"),
    literalSupplier = { 45358178 }
)