package app.revanced.patches.strava.upselling

import app.revanced.patcher.definingClass
import app.revanced.patcher.firstMethodComposite
import app.revanced.patcher.name
import app.revanced.patcher.opcodes
import com.android.tools.smali.dexlib2.Opcode

internal val getModulesMethodMatch = firstMethodComposite {
    name("getModules")
    definingClass { endsWith("/GenericLayoutEntry;") }
    opcodes(Opcode.IGET_OBJECT)
}
