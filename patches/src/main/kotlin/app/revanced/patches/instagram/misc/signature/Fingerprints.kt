package app.revanced.patches.instagram.misc.signature

import app.revanced.patcher.fingerprint
import com.android.tools.smali.dexlib2.Opcode

internal val launcherFingerprint = fingerprint {
    opcodes(
        Opcode.INVOKE_STATIC,
        Opcode.INVOKE_VIRTUAL,
        Opcode.RETURN_VOID
    )
    strings(
        "com.instagram.mainactivity.InstagramMainActivity",
        "redirect_from_launcher_activity"
    )
}

internal val onReceiveNotificationFingerprint = fingerprint {
    opcodes(
        Opcode.INVOKE_STATIC,
        Opcode.CONST,
        Opcode.GOTO
    )
    custom { method, classDef ->
        method.name == "onReceive" && classDef.endsWith("/NotificationActionReceiver;")
    }
}