package app.revanced.patches.strava.upselling

import app.revanced.patcher.definingClass
import app.revanced.patcher.gettingFirstMutableMethodDeclaratively
import app.revanced.patcher.name
import app.revanced.patcher.opcodes
import app.revanced.patcher.patch.BytecodePatchContext
import com.android.tools.smali.dexlib2.Opcode

internal val BytecodePatchContext.getModulesMethod by gettingFirstMutableMethodDeclaratively {
    name("getModules")
    definingClass("/GenericLayoutEntry;"::endsWith)
    opcodes(Opcode.IGET_OBJECT)
}
