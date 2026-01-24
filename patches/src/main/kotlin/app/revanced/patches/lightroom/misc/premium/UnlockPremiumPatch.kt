package app.revanced.patches.lightroom.misc.premium

import app.revanced.patcher.extensions.replaceInstruction
import app.revanced.patcher.patch.creatingBytecodePatch

@Suppress("unused", "ObjectPropertyName")
val `Unlock Premium` by creatingBytecodePatch {
    compatibleWith("com.adobe.lrmobile"("9.3.0"))

    apply {
        // Set hasPremium = true.
        hasPurchasedMethod.replaceInstruction(2, "const/4 v2, 0x1")
    }
}
