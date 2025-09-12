package app.revanced.patches.twitch.chat.antidelete

import app.revanced.patcher.fingerprint
import com.android.tools.smali.dexlib2.AccessFlags

internal val chatUtilCreateDeletedSpanFingerprint by fingerprint {
    custom { method, classDef ->
        classDef.endsWith("ChatUtil\$Companion;") && method.name == "createDeletedSpanFromChatMessageSpan"
    }
}

internal val deletedMessageClickableSpanCtorFingerprint by fingerprint {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.CONSTRUCTOR)
    custom { _, classDef ->
        classDef.endsWith("DeletedMessageClickableSpan;")
    }
}

internal val setHasModAccessFingerprint by fingerprint {
    custom { method, classDef ->
        classDef.endsWith("DeletedMessageClickableSpan;") && method.name == "setHasModAccess"
    }
}
