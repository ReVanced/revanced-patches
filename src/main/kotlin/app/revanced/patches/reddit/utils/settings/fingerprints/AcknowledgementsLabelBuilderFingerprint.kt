package app.revanced.patches.reddit.utils.settings.fingerprints

import app.revanced.patcher.extensions.or
import app.revanced.patcher.fingerprint.MethodFingerprint
import app.revanced.patches.reddit.utils.resourceid.SharedResourceIdPatch.LabelAcknowledgements
import app.revanced.util.containsWideLiteralInstructionIndex
import com.android.tools.smali.dexlib2.AccessFlags

object AcknowledgementsLabelBuilderFingerprint : MethodFingerprint(
    returnType = "Z",
    accessFlags = AccessFlags.PUBLIC or AccessFlags.FINAL,
    parameters = listOf("Landroidx/preference/Preference;"),
    customFingerprint = { methodDef, _ ->
        methodDef.definingClass.startsWith("Lcom/reddit/screen/settings/preferences/")
                && methodDef.containsWideLiteralInstructionIndex(LabelAcknowledgements)
    }
)