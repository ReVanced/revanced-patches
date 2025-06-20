package app.revanced.patches.spotify.misc.widgets

import app.revanced.patcher.fingerprint
import com.android.tools.smali.dexlib2.Opcode

internal val canBindAppWidgetPermissionFingerprint by fingerprint {
    strings("android.permission.BIND_APPWIDGET")
    opcodes(Opcode.AND_INT_LIT8)
}
