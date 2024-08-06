package app.revanced.patches.googlephotos.features

import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.InstructionExtensions.replaceInstruction
import app.revanced.patcher.patch.annotation.CompatiblePackage
import app.revanced.patcher.patch.annotation.Patch
import app.revanced.patches.all.misc.build.BaseSpoofBuildInfoPatch
import app.revanced.util.getReference
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.iface.instruction.OneRegisterInstruction
import com.android.tools.smali.dexlib2.iface.reference.StringReference

@Patch(
    name = "Enable unlimited storage",
    description = "Enable unlimited storage for photos and videos.",
    compatiblePackages = [CompatiblePackage("com.google.android.apps.photos")]
)
@Suppress("unused")
class EnableUnlimitedStoragePatch : BaseSpoofBuildInfoPatch() {
    override val brand = "google"
    override val manufacturer = "Google"
    override val device = "redfin"
    override val product = "redfin"
    override val model = "Pixel 5"
    override val fingerprint = "google/redfin/redfin:12/SP1A.211105.003/7757856:user/release-keys"

    override fun execute(context: BytecodeContext) {
        super.execute(context)

        context.findClass("Ladjg;")!!.mutableClass.methods.find { it.name == "<clinit>" }!!.let {
            val features = setOf(
                "com.google.android.apps.photos.NEXUS_PRELOAD",
                "com.google.android.apps.photos.PIXEL_2017_PRELOAD",
                "com.google.android.apps.photos.PIXEL_2018_PRELOAD",
                "com.google.android.apps.photos.PIXEL_2019_MIDYEAR_PRELOAD",
                "com.google.android.apps.photos.PIXEL_2019_PRELOAD",
                "com.google.android.feature.PIXEL_2020_MIDYEAR_EXPERIENCE",
                "com.google.android.feature.PIXEL_2020_EXPERIENCE"
            )

            it.implementation!!.instructions.filter { it.opcode == Opcode.CONST_STRING }.forEach { insn ->
                if (insn.getReference<StringReference>()!!.string !in features) return@forEach

                val register = (insn as OneRegisterInstruction).registerA

                it.replaceInstruction(
                    insn.location.index,
                    "const-string v$register, \"android.hardware.bluetooth\""
                )
            }
        }
    }
}