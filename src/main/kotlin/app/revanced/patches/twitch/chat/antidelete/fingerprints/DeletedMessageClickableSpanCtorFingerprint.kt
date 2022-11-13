package app.revanced.patches.twitch.chat.antidelete.fingerprints

import app.revanced.patcher.annotation.Name
import app.revanced.patcher.annotation.Version
import app.revanced.patcher.extensions.or

import app.revanced.patcher.fingerprint.method.impl.MethodFingerprint
import app.revanced.patches.twitch.chat.antidelete.annotations.ShowDeletedMessagesCompatibility
import org.jf.dexlib2.AccessFlags

@Name("deleted-msg-span-ctor-fingerprint")
@ShowDeletedMessagesCompatibility
@Version("0.0.1")
object DeletedMessageClickableSpanCtorFingerprint : MethodFingerprint(
    "V", AccessFlags.PUBLIC or AccessFlags.CONSTRUCTOR,
    customFingerprint = { methodDef ->
        methodDef.definingClass.endsWith("DeletedMessageClickableSpan;")
    }
)