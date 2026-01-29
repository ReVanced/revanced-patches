package app.revanced.patches.music.layout.buttons

import app.revanced.patcher.*
import app.revanced.patcher.extensions.instructions
import app.revanced.patcher.patch.BytecodePatchContext
import app.revanced.util.literal
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.Opcode

internal val BytecodePatchContext.mediaRouteButtonMethod by gettingFirstMutableMethodDeclaratively("MediaRouteButton") {
    accessFlags(AccessFlags.PRIVATE, AccessFlags.FINAL)
    returnType("Z")
}

internal val BytecodePatchContext.playerOverlayChipMethod by gettingFirstMutableMethodDeclaratively {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
    returnType("L")
    instructions(playerOverlayChip())
}

internal val BytecodePatchContext.historyMenuItemMethodMatch by composingFirstMethod {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
    returnType("V")
    parameterTypes("Landroid/view/Menu;")
    opcodes(
        Opcode.INVOKE_INTERFACE,
        Opcode.RETURN_VOID,
    )
    literal { historyMenuItem }
    custom {
        immutableClassDef.methods.count() == 5
    }
}

internal val BytecodePatchContext.historyMenuItemOfflineTabMethodMatch by composingFirstMethod {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
    returnType("V")
    parameterTypes("Landroid/view/Menu;")
    opcodes(
        Opcode.INVOKE_INTERFACE,
        Opcode.RETURN_VOID,
    )

    val match = indexedMatcher(items = unorderedAllOf(historyMenuItem(), offlineSettingsMenuItem()))
    custom { match(instructions) }
}

internal val BytecodePatchContext.searchActionViewMethod by gettingFirstMutableMethodDeclaratively {
    definingClass { endsWith("/SearchActionProvider;") }
    accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
    returnType("Landroid/view/View;")
    parameterTypes()
    literal { searchButton }
}

internal val BytecodePatchContext.topBarMenuItemImageViewMethod by gettingFirstMutableMethodDeclaratively {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
    returnType("Landroid/view/View;")
    parameterTypes()
    literal { topBarMenuItemImageView }
}
