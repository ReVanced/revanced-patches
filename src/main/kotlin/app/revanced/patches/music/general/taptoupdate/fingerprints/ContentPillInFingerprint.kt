package app.revanced.patches.music.general.taptoupdate.fingerprints

import app.revanced.patcher.extensions.or
import app.revanced.patcher.fingerprint.MethodFingerprint
import com.android.tools.smali.dexlib2.AccessFlags

object ContentPillInFingerprint : MethodFingerprint(
    returnType = "V",
    accessFlags = AccessFlags.PUBLIC or AccessFlags.FINAL,
    strings = listOf("MusicContentPillPresent", "Content pill VE is null", "refreshContentPillTopMargin")
)