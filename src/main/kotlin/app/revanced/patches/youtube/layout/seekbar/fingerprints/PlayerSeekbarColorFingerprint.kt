package app.revanced.patches.youtube.layout.seekbar.fingerprints

import app.revanced.patcher.fingerprint.methodFingerprint
import app.revanced.patches.youtube.layout.seekbar.inlineTimeBarColorizedBarPlayedColorDarkId
import app.revanced.patches.youtube.layout.seekbar.inlineTimeBarPlayedNotHighlightedColorId
import app.revanced.util.containsWideLiteralInstructionValue
import com.android.tools.smali.dexlib2.AccessFlags

internal val playerSeekbarColorFingerprint = methodFingerprint {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.CONSTRUCTOR)
    custom { method, _ ->
        method.containsWideLiteralInstructionValue(inlineTimeBarColorizedBarPlayedColorDarkId) &&
            method.containsWideLiteralInstructionValue(inlineTimeBarPlayedNotHighlightedColorId)
    }
}
