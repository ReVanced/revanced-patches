package app.revanced.patches.music.layout.castbutton

import app.revanced.patcher.fingerprint
import app.revanced.patcher.opcode
import app.revanced.patches.shared.misc.mapping.ResourceType
import app.revanced.patches.shared.misc.mapping.resourceLiteral
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.Opcode
import org.stringtemplate.v4.compiler.Bytecode.instructions

internal val mediaRouteButtonFingerprint by fingerprint {
    accessFlags(AccessFlags.PRIVATE, AccessFlags.FINAL)
    returns("Z")
    strings("MediaRouteButton")
}

internal val playerOverlayChipFingerprint by fingerprint {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
    returns("L")
    instructions(
        resourceLiteral(ResourceType.ID, "player_overlay_chip"),
        opcode(Opcode.MOVE_RESULT_OBJECT)
    )
}
