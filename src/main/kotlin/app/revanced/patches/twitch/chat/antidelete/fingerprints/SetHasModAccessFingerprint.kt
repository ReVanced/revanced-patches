package app.revanced.patches.twitch.chat.antidelete.fingerprints

import app.revanced.patcher.annotation.Name
import app.revanced.patcher.annotation.Version

import app.revanced.patcher.fingerprint.method.impl.MethodFingerprint
import app.revanced.patches.twitch.chat.antidelete.annotations.ShowDeletedMessagesCompatibility

@Name("has-mod-access-fingerprint")
@ShowDeletedMessagesCompatibility
@Version("0.0.1")
object SetHasModAccessFingerprint : MethodFingerprint(
    customFingerprint = { methodDef ->
        methodDef.definingClass.endsWith("DeletedMessageClickableSpan;") && methodDef.name == "setHasModAccess"
    }
)