package app.revanced.patches.youtube.general.trendingsearches.fingerprints

import app.revanced.patcher.extensions.or
import app.revanced.patcher.fingerprint.MethodFingerprint
import app.revanced.patches.youtube.utils.resourceid.SharedResourceIdPatch.YtOutlineArrowTimeBlack
import app.revanced.patches.youtube.utils.resourceid.SharedResourceIdPatch.YtOutlineFireBlack
import app.revanced.patches.youtube.utils.resourceid.SharedResourceIdPatch.YtOutlineSearchBlack
import app.revanced.util.containsWideLiteralInstructionIndex
import com.android.tools.smali.dexlib2.AccessFlags

object SearchBarEntryFingerprint : MethodFingerprint(
    returnType = "L",
    accessFlags = AccessFlags.PUBLIC or AccessFlags.FINAL,
    customFingerprint = { methodDef, _ ->
        methodDef.containsWideLiteralInstructionIndex(YtOutlineArrowTimeBlack)
                && methodDef.containsWideLiteralInstructionIndex(YtOutlineFireBlack)
                && methodDef.containsWideLiteralInstructionIndex(YtOutlineSearchBlack)
    }
)