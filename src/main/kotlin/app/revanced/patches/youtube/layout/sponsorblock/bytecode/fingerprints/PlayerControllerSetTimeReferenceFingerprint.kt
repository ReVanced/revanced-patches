package app.revanced.patches.youtube.layout.sponsorblock.bytecode.fingerprints

import app.revanced.patcher.annotation.Name
import app.revanced.patcher.annotation.Version

import app.revanced.patcher.fingerprint.method.impl.MethodFingerprint
import app.revanced.patches.youtube.layout.sponsorblock.annotations.SponsorBlockCompatibility
import org.jf.dexlib2.Opcode

@Name("player-controller-set-time-reference-fingerprint")

@SponsorBlockCompatibility
@Version("0.0.1")
object PlayerControllerSetTimeReferenceFingerprint : MethodFingerprint(
    opcodes = listOf(Opcode.INVOKE_DIRECT_RANGE, Opcode.IGET_OBJECT),
    strings = listOf("Media progress reported outside media playback: ")
)