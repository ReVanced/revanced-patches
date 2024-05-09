package app.revanced.patches.piccomafr.misc

import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.InstructionExtensions.replaceInstruction
import app.revanced.patcher.extensions.or
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.PatchException
import app.revanced.patcher.patch.annotation.CompatiblePackage
import app.revanced.patcher.patch.annotation.Patch
import app.revanced.patcher.util.proxy.mutableTypes.MutableField.Companion.toMutable
import app.revanced.patches.piccomafr.misc.fingerprints.AppConfFingerprint
import app.revanced.patches.piccomafr.misc.fingerprints.OpenToUserFingerprint
import app.revanced.util.exception
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.immutable.ImmutableField
import com.android.tools.smali.dexlib2.immutable.value.ImmutableBooleanEncodedValue


@Patch(
    name = "Enable Debug",
    description = "Enable every debug possibilities of the app.",
    compatiblePackages = [CompatiblePackage(
        "com.piccomaeurope.fr",
        [
            "6.4.0", "6.4.1", "6.4.2", "6.4.3", "6.4.4", "6.4.5",
            "6.5.0", "6.5.1", "6.5.2", "6.5.3", "6.5.4",
            "6.6.0", "6.6.1", "6.6.2"
        ],
    )],
    use = false
)
@Suppress("unused")
object EnableDebugPatch : BytecodePatch(
    setOf(AppConfFingerprint, OpenToUserFingerprint),
) {
    override fun execute(context: BytecodeContext) {

        val appConfInit = AppConfFingerprint.result?.mutableMethod?: throw AppConfFingerprint.exception
        val className = appConfInit.definingClass.removeSuffix(";")

        val instruction = "sget-object v0, $className\$a;->v:$className\$a;"
        appConfInit.replaceInstruction(0,instruction)

        // ====

        val buildConfInit = OpenToUserFingerprint.result?.mutableMethod?: throw OpenToUserFingerprint.exception
        buildConfInit.replaceInstruction(
            0, "sget-object v0, Ljava/lang/Boolean;->FALSE:Ljava/lang/Boolean;")

        // ====

        val volleyBuildConfig = context.findClass("Lcom/android/volley/BuildConfig;")
            ?: throw PatchException("Volley build Config not found")

        val instanceField = ImmutableField(
            volleyBuildConfig.immutableClass.type,
            "DEBUG",
            "Z",
            AccessFlags.PUBLIC or AccessFlags.STATIC or AccessFlags.FINAL,
            ImmutableBooleanEncodedValue.TRUE_VALUE,
            null,
            null
        ).toMutable()

        volleyBuildConfig.mutableClass.staticFields.remove(instanceField)
        volleyBuildConfig.mutableClass.staticFields.add(instanceField)

    }
}
