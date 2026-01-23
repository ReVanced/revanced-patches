package app.revanced.patches.music.layout.buttons

import app.revanced.patcher.fingerprint
import app.revanced.util.containsLiteralInstruction
import app.revanced.util.literal
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.Opcode

internal val mediaRouteButtonFingerprint = fingerprint {
    accessFlags(AccessFlags.PRIVATE, AccessFlags.FINAL)
    returns("Z")
    strings("MediaRouteButton")
}

internal val playerOverlayChipFingerprint = fingerprint {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
    returns("L")
    literal { playerOverlayChip }
}

internal val BytecodePatchContext.historyMenuItemMethod by gettingFirstMutableMethodDeclaratively {
    definingClass {
        it.methods.count()
        methods.count()
    }
    accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
    returnType("V")
    parameterTypes("Landroid/view/Menu;")
    opcodes(
        Opcode.INVOKE_INTERFACE,
        Opcode.RETURN_VOID,
    )
    historyMenuItem()
    custom { _, classDef ->
        classDef.methods.count() == 5
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
