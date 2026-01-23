package app.revanced.patches.music.layout.buttons

import app.revanced.patcher.accessFlags
import app.revanced.patcher.custom
import app.revanced.patcher.definingClass
import app.revanced.patcher.extensions.instructions
import app.revanced.patcher.gettingFirstMutableMethodDeclaratively
import app.revanced.patcher.immutableClassDef
import app.revanced.patcher.instructions
import app.revanced.patcher.invoke
import app.revanced.patcher.literal
import app.revanced.patcher.matchIndexed
import app.revanced.patcher.opcodes
import app.revanced.patcher.parameterTypes
import app.revanced.patcher.patch.BytecodePatchContext
import app.revanced.patcher.returnType
import app.revanced.patcher.unorderedAllOf
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.Opcode

internal val BytecodePatchContext.mediaRouteButtonMethod by gettingFirstMutableMethodDeclaratively("MediaRouteButton") {
    accessFlags(AccessFlags.PRIVATE, AccessFlags.FINAL)
    returnType("Z")
}

internal val BytecodePatchContext.playerOverlayChipMethod by gettingFirstMutableMethodDeclaratively {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
    returnType("L")
    custom {
        instructions { literal { playerOverlayChip() } }
    }
}

internal val BytecodePatchContext.historyMenuItemMethod by gettingFirstMutableMethodDeclaratively {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
    returnType("V")
    parameterTypes("Landroid/view/Menu;")
    opcodes(
        Opcode.INVOKE_INTERFACE,
        Opcode.RETURN_VOID,
    )
    historyMenuItem()
    custom {
        immutableClassDef.methods.count() == 5 // TODO CONFIRM
    }
}

internal val BytecodePatchContext.historyMenuItemOfflineTabMethod by gettingFirstMutableMethodDeclaratively {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
    returnType("V")
    parameterTypes("Landroid/view/Menu;")
    opcodes(
        Opcode.INVOKE_INTERFACE,
        Opcode.RETURN_VOID,
    )
    custom {
        instructions.matchIndexed("literals", items = unorderedAllOf(historyMenuItem(), offlineSettingsMenuItem()))
    }
}

internal val BytecodePatchContext.searchActionViewMethod by gettingFirstMutableMethodDeclaratively {
    definingClass("/SearchActionProvider;"::endsWith)
    accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
    returnType("Landroid/view/View;")
    parameterTypes()
    searchButton()
}

internal val BytecodePatchContext.topBarMenuItemImageViewMethod by gettingFirstMutableMethodDeclaratively {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
    returnType("Landroid/view/View;")
    parameterTypes()
    topBarMenuItemImageView()
}
