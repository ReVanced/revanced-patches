package app.revanced.patches.youtube.utils.sponsorblock.fingerprints

import app.revanced.patcher.extensions.or
import app.revanced.patcher.fingerprint.MethodFingerprint
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.Opcode

object SegmentPlaybackControllerFingerprint : MethodFingerprint(
    returnType = "V",
    accessFlags = AccessFlags.PUBLIC or AccessFlags.STATIC,
    parameters = listOf("Ljava/lang/Object;"),
    opcodes = listOf(Opcode.CONST_STRING),
    customFingerprint = { methodDef, _ ->
        methodDef.definingClass == "Lapp/revanced/integrations/youtube/sponsorblock/SegmentPlaybackController;"
                && methodDef.name == "setSponsorBarRect"
    }
)
