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
    name = "Enable debugging",
    description = "Enable every debug possibilities of the app.",
    compatiblePackages = [
        CompatiblePackage(
            "com.piccomaeurope.fr",
            [
                "6.4.0",
                "6.4.1",
                "6.4.2",
                "6.4.3",
                "6.4.4",
                "6.4.5",
                "6.5.0",
                "6.5.1",
                "6.5.2",
                "6.5.3",
                "6.5.4",
                "6.6.0",
                "6.6.1",
                "6.6.2",
            ],
        ),
    ],
    use = false,
)
@Suppress("unused")
object EnableDebuggingPatch : BytecodePatch(
    setOf(AppConfFingerprint, OpenToUserFingerprint),
) {
    override fun execute(context: BytecodeContext) {
        AppConfFingerprint.result?.mutableMethod?.apply {
            val className = definingClass.removeSuffix(";")

            replaceInstruction(0, "sget-object v0, $className\$a;->v:$className\$a;")
        } ?: throw AppConfFingerprint.exception

        OpenToUserFingerprint.result?.mutableMethod?.replaceInstruction(
            0,
            "sget-object v0, Ljava/lang/Boolean;->FALSE:Ljava/lang/Boolean;",
        ) ?: throw OpenToUserFingerprint.exception

        with(context.findClass("Lcom/android/volley/BuildConfig;")) {
            this ?: throw PatchException("Volley build Config not found")

            val instanceField = ImmutableField(
                immutableClass.type,
                "DEBUG",
                "Z",
                AccessFlags.PUBLIC or AccessFlags.STATIC or AccessFlags.FINAL,
                ImmutableBooleanEncodedValue.TRUE_VALUE,
                null,
                null,
            ).toMutable()

            mutableClass.staticFields.apply {
                remove(instanceField)
                add(instanceField)
            }
        }
    }
}
