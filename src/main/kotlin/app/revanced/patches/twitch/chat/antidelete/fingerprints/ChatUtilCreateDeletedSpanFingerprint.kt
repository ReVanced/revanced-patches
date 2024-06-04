package app.revanced.patches.twitch.chat.antidelete.fingerprints

import app.revanced.patcher.fingerprint.methodFingerprint

internal val chatUtilCreateDeletedSpanFingerprint = methodFingerprint {
    custom { methodDef, classDef ->
        classDef.endsWith("ChatUtil\$Companion;") && methodDef.name == "createDeletedSpanFromChatMessageSpan"
    }
}
