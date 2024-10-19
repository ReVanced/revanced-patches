package app.revanced.patches.youtube.layout.miniplayer.fingerprints

import app.revanced.patcher.extensions.or
import app.revanced.patcher.fingerprint.MethodFingerprint
import app.revanced.patches.youtube.layout.miniplayer.MiniplayerResourcePatch
import app.revanced.util.containsWideLiteralInstructionValue
import com.android.tools.smali.dexlib2.AccessFlags

internal object MiniplayerMinimumSizeFingerprint : MethodFingerprint(
    accessFlags = AccessFlags.PUBLIC or AccessFlags.CONSTRUCTOR,
    customFingerprint = { methodDef, _ ->
        methodDef.containsWideLiteralInstructionValue(192)
                && methodDef.containsWideLiteralInstructionValue(128)
                && methodDef.containsWideLiteralInstructionValue(MiniplayerResourcePatch.miniplayerMaxSize)
    }
)