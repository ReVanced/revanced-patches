package app.revanced.patches.instagram.hide.stories
import app.revanced.patcher.fingerprint
import com.android.tools.smali.dexlib2.Opcode


internal val getOrCreateAvatarViewFingerprint = fingerprint {
        parameters()
        returns("L")
        custom { method, classDef ->
            classDef.type == "Lcom/instagram/reels/ui/views/reelavatar/RecyclerReelAvatarView;"
        }
        opcodes(
            Opcode.INVOKE_VIRTUAL,
            Opcode.IPUT_OBJECT,
            Opcode.INVOKE_VIRTUAL // Add View (Story)
        )
    }
