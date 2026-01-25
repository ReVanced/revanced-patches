package app.revanced.patches.viber.ads

import app.revanced.patcher.firstMethodComposite
import app.revanced.patcher.instructions
import app.revanced.patcher.invoke
import com.android.tools.smali.dexlib2.Opcode

internal val findAdStringMethodMatch = firstMethodComposite {
    instructions(
        Opcode.NEW_INSTANCE(),
        "viber_plus_debug_ads_free_flag"(),
    )
}
