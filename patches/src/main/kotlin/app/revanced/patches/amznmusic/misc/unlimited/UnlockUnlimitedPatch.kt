package app.revanced.patches.amznmusic.misc.unlimited

import app.revanced.patcher.extensions.addInstructions
import app.revanced.patcher.extensions.getInstruction
import app.revanced.patcher.firstMethod
import app.revanced.patcher.patch.bytecodePatch
import app.revanced.patches.amznmusic.misc.extension.sharedExtensionPatch
import app.revanced.util.getReference
import app.revanced.util.indexOfFirstInstructionOrThrow
import app.revanced.util.registersUsed
import com.android.tools.smali.dexlib2.iface.reference.FieldReference

@Suppress("unused")
val unlockUnlimitedPatch = bytecodePatch(
    name = "Unlock Unlimited",
    description = "Unlock Music Unlimited features.",
) {
    compatibleWith("com.amazon.mp3")

    dependsOn(sharedExtensionPatch)

    apply {
        val userCtor = firstMethod { name == "<init>" && definingClass == "Lcom/amazon/music/account/User;" }

        userCtor.apply {
            val benefitsInstructionIndex = indexOfFirstInstructionOrThrow {
                getReference<FieldReference>()?.name == "benefits"
            }
            assert(benefitsInstructionIndex > 0)

            val register = getInstruction(benefitsInstructionIndex).registersUsed[0]
            addInstructions(benefitsInstructionIndex,
                """
                    invoke-static {}, Lapp/revanced/extension/amznmusic/patches/UnlockUnlimitedPatch;->createBenefitSet()Ljava/util/Set;
                    move-result-object p$register
                """)
        }
    }
}

