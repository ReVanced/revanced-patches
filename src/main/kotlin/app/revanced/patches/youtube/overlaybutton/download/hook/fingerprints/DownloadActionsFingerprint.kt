package app.revanced.patches.youtube.overlaybutton.download.hook.fingerprints

import app.revanced.patcher.extensions.or
import app.revanced.patcher.fingerprint.MethodFingerprint
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.Opcode

object DownloadActionsFingerprint : MethodFingerprint(
    returnType = "V",
    accessFlags = AccessFlags.PUBLIC or AccessFlags.CONSTRUCTOR,
    parameters = listOf("L", "L", "Z"),
    opcodes = listOf(Opcode.INVOKE_STATIC),
    strings = listOf("offline/get_download_action")
)
