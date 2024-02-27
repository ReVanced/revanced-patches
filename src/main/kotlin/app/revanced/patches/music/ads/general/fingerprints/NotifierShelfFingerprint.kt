package app.revanced.patches.music.ads.general.fingerprints

import app.revanced.patcher.extensions.or
import app.revanced.patcher.fingerprint.MethodFingerprint
import app.revanced.patches.music.utils.resourceid.SharedResourceIdPatch.ButtonContainer
import app.revanced.patches.music.utils.resourceid.SharedResourceIdPatch.MusicNotifierShelf
import app.revanced.util.containsWideLiteralInstructionIndex
import com.android.tools.smali.dexlib2.AccessFlags

object NotifierShelfFingerprint : MethodFingerprint(
    returnType = "V",
    accessFlags = AccessFlags.PUBLIC or AccessFlags.CONSTRUCTOR,
    customFingerprint = { methodDef, _ ->
        methodDef.containsWideLiteralInstructionIndex(MusicNotifierShelf)
                && methodDef.containsWideLiteralInstructionIndex(ButtonContainer)
    }
)