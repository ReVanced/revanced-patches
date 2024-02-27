package app.revanced.patches.reddit.layout.subredditdialog.fingerprints

import app.revanced.patcher.extensions.or
import app.revanced.patcher.fingerprint.MethodFingerprint
import app.revanced.patches.reddit.utils.resourceid.SharedResourceIdPatch.TextAppearanceRedditBaseOldButtonColored
import app.revanced.util.containsWideLiteralInstructionIndex
import com.android.tools.smali.dexlib2.AccessFlags

object RedditAlertDialogsFingerprint : MethodFingerprint(
    returnType = "V",
    accessFlags = AccessFlags.PUBLIC or AccessFlags.FINAL,
    customFingerprint = { methodDef, classDef ->
        methodDef.containsWideLiteralInstructionIndex(TextAppearanceRedditBaseOldButtonColored)
                && classDef.sourceFile == "RedditAlertDialogs.kt"
    }
)