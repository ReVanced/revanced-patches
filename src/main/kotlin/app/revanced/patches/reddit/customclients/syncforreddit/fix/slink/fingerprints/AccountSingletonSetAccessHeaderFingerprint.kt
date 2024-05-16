package app.revanced.patches.reddit.customclients.syncforreddit.fix.slink.fingerprints

import app.revanced.patcher.fingerprint.MethodFingerprint
import com.android.tools.smali.dexlib2.AccessFlags

internal object AccountSingletonSetAccessHeaderFingerprint: MethodFingerprint(
    strings = listOf("Authorization", "bearer "),
    returnType = "Ljava/util/HashMap;",
    customFingerprint = { methodDef, _ -> methodDef.definingClass == "Lcom/laurencedawson/reddit_sync/singleton/a;" }
)