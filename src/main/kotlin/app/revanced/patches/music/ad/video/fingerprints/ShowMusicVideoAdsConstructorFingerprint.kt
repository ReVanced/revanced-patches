package app.revanced.patches.music.ad.video.fingerprints
import app.revanced.patcher.extensions.or
import app.revanced.patcher.fingerprint.method.annotation.FuzzyPatternScanMethod
import app.revanced.patcher.fingerprint.method.impl.MethodFingerprint
import org.jf.dexlib2.AccessFlags
import org.jf.dexlib2.Opcode

@FuzzyPatternScanMethod(2) // FIXME: Test this threshold and find the best value.
object ShowMusicVideoAdsConstructorFingerprint : MethodFingerprint(
    "V", AccessFlags.PUBLIC or AccessFlags.CONSTRUCTOR, listOf("L", "L", "L"), listOf(
        Opcode.INVOKE_DIRECT,
        Opcode.NEW_INSTANCE,
        Opcode.INVOKE_DIRECT,
        Opcode.IPUT_OBJECT,
        Opcode.NEW_INSTANCE,
        Opcode.INVOKE_DIRECT,
        Opcode.IPUT_OBJECT,
        Opcode.NEW_INSTANCE,
        Opcode.INVOKE_DIRECT,
        Opcode.IPUT_OBJECT,
        Opcode.IPUT_OBJECT,
        Opcode.IPUT_OBJECT,
        Opcode.CONST_4,
        Opcode.IPUT_BOOLEAN,
        Opcode.RETURN_VOID
    )
)
