package app.revanced.patches.twitch.chat.antidelete

import com.android.tools.smali.dexlib2.AccessFlags
import app.revanced.patcher.fingerprint.methodFingerprint

internal val chatUtilCreateDeletedSpanFingerprint = methodFingerprint {
    custom { methodDef, classDef ->
        classDef.endsWith("ChatUtil\$Companion;") && methodDef.name == "createDeletedSpanFromChatMessageSpan"
    }
}

internal val deletedMessageClickableSpanCtorFingerprint = methodFingerprint {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.CONSTRUCTOR)
    returns("V")
    custom { _, classDef ->
        classDef.endsWith("DeletedMessageClickableSpan;")
    }
}

internal val setHasModAccessFingerprint = methodFingerprint {
    custom { methodDef, classDef ->
        classDef.endsWith("DeletedMessageClickableSpan;") && methodDef.name == "setHasModAccess"
    }
}
