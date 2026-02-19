package app.revanced.patches.twitch.chat.antidelete

import app.revanced.patcher.accessFlags
import app.revanced.patcher.definingClass
import app.revanced.patcher.gettingFirstMethodDeclaratively
import app.revanced.patcher.name
import app.revanced.patcher.patch.BytecodePatchContext
import com.android.tools.smali.dexlib2.AccessFlags

internal val BytecodePatchContext.chatUtilCreateDeletedSpanMethod by gettingFirstMethodDeclaratively {
    name("createDeletedSpanFromChatMessageSpan")
    definingClass { endsWith("ChatUtil\$Companion;") }
}

internal val BytecodePatchContext.deletedMessageClickableSpanCtorMethod by gettingFirstMethodDeclaratively {
    definingClass { endsWith("DeletedMessageClickableSpan;") }
    accessFlags(AccessFlags.PUBLIC, AccessFlags.CONSTRUCTOR)
}

internal val BytecodePatchContext.setHasModAccessMethod by gettingFirstMethodDeclaratively {
    name("setHasModAccess")
    definingClass { endsWith("DeletedMessageClickableSpan;") }
}
