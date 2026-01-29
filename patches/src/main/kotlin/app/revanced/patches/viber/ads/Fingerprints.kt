package app.revanced.patches.viber.ads

import app.revanced.patcher.composingFirstMethod
import app.revanced.patcher.instructions
import app.revanced.patcher.invoke
import app.revanced.patcher.patch.BytecodePatchContext
import com.android.tools.smali.dexlib2.Opcode

internal val BytecodePatchContext.findAdStringMethodMatch by composingFirstMethod {
    instructions(
        Opcode.NEW_INSTANCE(),
        "viber_plus_debug_ads_free_flag"(),
    )
}
