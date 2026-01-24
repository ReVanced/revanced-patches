package app.revanced.patches.mifitness.misc.login

import app.revanced.patcher.extensions.addInstruction
import app.revanced.patcher.patch.creatingBytecodePatch

@Suppress("ObjectPropertyName")
val `Fix login` by creatingBytecodePatch(
    description = "Fixes login for uncertified Mi Fitness app",
) {
    compatibleWith("com.xiaomi.wearable")

    apply {
        xiaomiAccountManagerConstructorMethod.addInstruction(0, "const/16 p2, 0x0")
    }
}
