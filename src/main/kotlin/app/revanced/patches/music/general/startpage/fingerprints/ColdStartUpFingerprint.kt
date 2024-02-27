package app.revanced.patches.music.general.startpage.fingerprints

import app.revanced.patcher.extensions.or
import app.revanced.patcher.fingerprint.MethodFingerprint
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.Opcode

object ColdStartUpFingerprint : MethodFingerprint(
    returnType = "Ljava/lang/String;",
    accessFlags = AccessFlags.PUBLIC or AccessFlags.FINAL,
    parameters = emptyList(),
    opcodes = listOf(
        Opcode.GOTO,
        Opcode.CONST_STRING,
        Opcode.RETURN_OBJECT
    ),
    strings = listOf("FEmusic_library_sideloaded_tracks", "FEmusic_home")
)
