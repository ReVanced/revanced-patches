package app.revanced.patches.strava.upselling

import app.revanced.patcher.composingFirstMethod
import app.revanced.patcher.definingClass
import app.revanced.patcher.name
import app.revanced.patcher.opcodes
import app.revanced.patcher.patch.BytecodePatchContext
import com.android.tools.smali.dexlib2.Opcode

internal val BytecodePatchContext.getModulesMethodMatch by composingFirstMethod {
    name("getModules")
    definingClass("/GenericLayoutEntry;")
    opcodes(Opcode.IGET_OBJECT)
}
