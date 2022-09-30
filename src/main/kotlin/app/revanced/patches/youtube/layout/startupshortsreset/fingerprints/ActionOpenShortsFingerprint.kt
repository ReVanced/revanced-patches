package app.revanced.patches.youtube.layout.startupshortsreset.fingerprints

import app.revanced.patcher.annotation.Name
import app.revanced.patcher.annotation.Version
import app.revanced.patcher.extensions.or
import app.revanced.patcher.fingerprint.method.annotation.MatchingMethod
import app.revanced.patcher.fingerprint.method.impl.MethodFingerprint
import app.revanced.patches.youtube.layout.startupshortsreset.annotations.StartupShortsResetCompatibility
import org.jf.dexlib2.AccessFlags
import org.jf.dexlib2.Opcode

@Name("action-open-shorts-fingerprint")
@MatchingMethod("Lkyt;", "l")
@StartupShortsResetCompatibility
@Version("0.0.1")
object ActionOpenShortsFingerprint : MethodFingerprint(
    "V", AccessFlags.PUBLIC or AccessFlags.FINAL, listOf("L", "L"),
    strings = listOf("com.google.android.youtube.action.open.shorts"),
)