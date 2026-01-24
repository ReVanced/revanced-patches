package app.revanced.patches.twitch.chat.antidelete

import app.revanced.patcher.accessFlags
import app.revanced.patcher.definingClass
import app.revanced.patcher.gettingFirstMutableMethodDeclaratively
import app.revanced.patcher.name
import app.revanced.patcher.patch.BytecodePatchContext
import com.android.tools.smali.dexlib2.AccessFlags

internal val BytecodePatchContext.chatUtilCreateDeletedSpanMethod by gettingFirstMutableMethodDeclaratively {
    name("createDeletedSpanFromChatMessageSpan")
    definingClass { endsWith("ChatUtil\$Companion;") }
}

internal val BytecodePatchContext.deletedMessageClickableSpanCtorMethod by gettingFirstMutableMethodDeclaratively {
    definingClass { endsWith("DeletedMessageClickableSpan;") }
    accessFlags(AccessFlags.PUBLIC, AccessFlags.CONSTRUCTOR)
}

internal val BytecodePatchContext.setHasModAccessMethod by gettingFirstMutableMethodDeclaratively {
    name("setHasModAccess")
    definingClass { endsWith("DeletedMessageClickableSpan;") }
}
