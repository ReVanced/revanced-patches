package app.revanced.patches.youtube.general.searchterm.fingerprints

import app.revanced.patcher.extensions.or
import app.revanced.patcher.fingerprint.MethodFingerprint
import com.android.tools.smali.dexlib2.AccessFlags

object CreateSearchSuggestionsFingerprint : MethodFingerprint(
    returnType = "Landroid/view/View;",
    accessFlags = AccessFlags.PUBLIC or AccessFlags.FINAL,
    parameters = listOf("I", "Landroid/view/View;", "Landroid/view/ViewGroup;"),
    strings = listOf("ss_rds")
)