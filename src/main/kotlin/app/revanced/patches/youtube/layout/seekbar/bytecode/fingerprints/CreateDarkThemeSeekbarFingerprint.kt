package app.revanced.patches.youtube.layout.seekbar.bytecode.fingerprints

import app.revanced.patcher.extensions.or
import app.revanced.patcher.fingerprint.method.impl.MethodFingerprint
import app.revanced.patches.youtube.layout.seekbar.resource.SeekbarColorResourcePatch
import app.revanced.util.patch.indexOfFirstConstantInstruction
import org.jf.dexlib2.AccessFlags

object CreateDarkThemeSeekbarFingerprint : MethodFingerprint(
    accessFlags = AccessFlags.PUBLIC or AccessFlags.CONSTRUCTOR,
    customFingerprint = { method, _ ->
        method.indexOfFirstConstantInstruction(SeekbarColorResourcePatch.inlineTimeBarColorizedBarPlayedColorDarkId) != -1
                && method.indexOfFirstConstantInstruction(SeekbarColorResourcePatch.inlineTimeBarPlayedNotHighlightedColorId) != -1
    }
)