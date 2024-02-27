package app.revanced.patches.youtube.layout.pipnotification

import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.InstructionExtensions.addInstruction
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.PatchException
import app.revanced.patcher.patch.annotation.CompatiblePackage
import app.revanced.patcher.patch.annotation.Patch
import app.revanced.patches.youtube.layout.pipnotification.fingerprints.PiPNotificationFingerprint
import app.revanced.patches.youtube.utils.resourceid.SharedResourceIdPatch
import app.revanced.patches.youtube.utils.settings.SettingsPatch
import app.revanced.util.exception
import com.android.tools.smali.dexlib2.iface.instruction.ReferenceInstruction

@Patch(
    name = "Disable pip notification",
    description = "Disable pip notification when you first launch pip mode.",
    dependencies =
    [
        SettingsPatch::class,
        SharedResourceIdPatch::class
    ],
    compatiblePackages = [
        CompatiblePackage(
            "com.google.android.youtube",
            [
                "18.25.40",
                "18.27.36",
                "18.29.38",
                "18.30.37",
                "18.31.40",
                "18.32.39",
                "18.33.40",
                "18.34.38",
                "18.35.36",
                "18.36.39",
                "18.37.36",
                "18.38.44",
                "18.39.41",
                "18.40.34",
                "18.41.39",
                "18.42.41",
                "18.43.45",
                "18.44.41",
                "18.45.43",
                "18.46.45",
                "18.48.39",
                "18.49.37",
                "19.01.34",
                "19.02.39"
            ]
        )
    ]
)
@Suppress("unused")
object PiPNotificationPatch : BytecodePatch(
    setOf(PiPNotificationFingerprint)
) {
    override fun execute(context: BytecodeContext) {

        PiPNotificationFingerprint.result?.let {
            it.mutableMethod.apply {
                val checkCastCalls = implementation!!.instructions.withIndex()
                    .filter { instruction ->
                        (instruction.value as? ReferenceInstruction)?.reference.toString() == "Lcom/google/apps/tiktok/account/AccountId;"
                    }

                if (checkCastCalls.size != 3)
                    throw PatchException("Couldn't find target Index")

                arrayOf(
                    checkCastCalls.elementAt(1).index,
                    checkCastCalls.elementAt(0).index
                ).forEach { index ->
                    addInstruction(
                        index + 1,
                        "return-void"
                    )
                }
            }
        } ?: throw PiPNotificationFingerprint.exception

        /**
         * Add settings
         */
        SettingsPatch.updatePatchStatus("Disable pip notification")

    }
}