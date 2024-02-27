package app.revanced.patches.youtube.flyoutpanel.player.fingerprints

import app.revanced.patcher.extensions.or
import app.revanced.patcher.fingerprint.MethodFingerprint
import app.revanced.patches.youtube.utils.resourceid.SharedResourceIdPatch.BottomSheetFooterText
import app.revanced.patches.youtube.utils.resourceid.SharedResourceIdPatch.SubtitleMenuSettingsFooterInfo
import app.revanced.util.containsWideLiteralInstructionIndex
import com.android.tools.smali.dexlib2.AccessFlags

object CaptionsBottomSheetFingerprint : MethodFingerprint(
    accessFlags = AccessFlags.PUBLIC or AccessFlags.FINAL,
    customFingerprint = { methodDef, _ ->
        methodDef.containsWideLiteralInstructionIndex(BottomSheetFooterText)
                && methodDef.containsWideLiteralInstructionIndex(SubtitleMenuSettingsFooterInfo)
    }
)