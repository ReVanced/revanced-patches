package app.revanced.patches.spotify.misc.widgets

import app.revanced.patcher.gettingFirstMutableMethodDeclaratively
import app.revanced.patcher.opcodes
import app.revanced.patcher.patch.BytecodePatchContext
import com.android.tools.smali.dexlib2.Opcode

internal val BytecodePatchContext.canBindAppWidgetPermissionMethod by gettingFirstMutableMethodDeclaratively("android.permission.BIND_APPWIDGET") {
    opcodes(Opcode.AND_INT_LIT8)
}
