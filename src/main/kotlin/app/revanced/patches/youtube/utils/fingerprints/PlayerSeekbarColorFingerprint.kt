package app.revanced.patches.youtube.utils.fingerprints

import app.revanced.patcher.extensions.or
import app.revanced.patcher.fingerprint.MethodFingerprint
import app.revanced.patches.youtube.utils.resourceid.SharedResourceIdPatch.InlineTimeBarColorizedBarPlayedColorDark
import app.revanced.patches.youtube.utils.resourceid.SharedResourceIdPatch.InlineTimeBarPlayedNotHighlightedColor
import app.revanced.util.containsWideLiteralInstructionIndex
import com.android.tools.smali.dexlib2.AccessFlags

object PlayerSeekbarColorFingerprint : MethodFingerprint(
    returnType = "V",
    accessFlags = AccessFlags.PUBLIC or AccessFlags.CONSTRUCTOR,
    customFingerprint = { methodDef, _ ->
        methodDef.containsWideLiteralInstructionIndex(InlineTimeBarColorizedBarPlayedColorDark)
                && methodDef.containsWideLiteralInstructionIndex(
            InlineTimeBarPlayedNotHighlightedColor
        )
    }
)