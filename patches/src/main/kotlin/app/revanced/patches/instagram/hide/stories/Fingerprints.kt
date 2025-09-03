package app.revanced.patches.instagram.hide.stories
import app.revanced.patcher.fingerprint
import com.android.tools.smali.dexlib2.Opcode


internal val getOrCreateAvatarViewFingerprint = fingerprint {
        custom { method, classDef ->
            classDef.toString() == "Lcom/instagram/reels/ui/views/reelavatar/RecyclerReelAvatarView;"
        }
        opcodes(
            Opcode.INVOKE_VIRTUAL,
            Opcode.IPUT_OBJECT,
            Opcode.INVOKE_VIRTUAL // Add View (Story)
        )
    }
