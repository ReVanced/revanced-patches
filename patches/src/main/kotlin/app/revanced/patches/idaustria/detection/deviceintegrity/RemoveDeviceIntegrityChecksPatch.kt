package app.revanced.patches.idaustria.detection.deviceintegrity

import app.revanced.patcher.extensions.addInstructions
import app.revanced.patcher.patch.bytecodePatch
import app.revanced.util.returnEarly

@Suppress("unused")
val removeDeviceIntegrityChecksPatch = bytecodePatch(
    name = "Remove device integrity checks",
    description = "Removes the check for root permissions and unlocked bootloader.",
) {
    compatibleWith("at.gv.oe.app")

    apply {
        isDeviceRootedMethod.returnEarly(false)

        isDeviceBootloaderOpenMethod.apply {
            addInstructions(
                0,
                """
                    const/4 v0, 0x0
                    invoke-static { v0 }, Lkotlin/coroutines/jvm/internal/Boxing;->boxBoolean(Z)Ljava/lang/Boolean;
                    move-result-object v0
                    return-object v0
                """
            )
        }
    }
}
