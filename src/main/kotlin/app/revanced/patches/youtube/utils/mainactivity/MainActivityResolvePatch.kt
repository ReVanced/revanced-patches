package app.revanced.patches.youtube.utils.mainactivity

import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.InstructionExtensions.addInstruction
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.PatchException
import app.revanced.patcher.util.proxy.mutableTypes.MutableClass
import app.revanced.patcher.util.proxy.mutableTypes.MutableMethod
import app.revanced.patches.youtube.utils.integrations.Constants.UTILS_PATH
import app.revanced.patches.youtube.utils.mainactivity.fingerprints.MainActivityFingerprint
import app.revanced.util.exception
import com.android.tools.smali.dexlib2.iface.ClassDef

object MainActivityResolvePatch : BytecodePatch(
    setOf(MainActivityFingerprint)
) {
    lateinit var mainActivityClassDef: ClassDef
    lateinit var mainActivityMutableClass: MutableClass
    lateinit var onBackPressedMethod: MutableMethod
    private lateinit var onCreateMethod: MutableMethod

    override fun execute(context: BytecodeContext) {
        MainActivityFingerprint.result?.let {
            mainActivityClassDef = it.classDef
            mainActivityMutableClass = it.mutableClass
            onBackPressedMethod =
                mainActivityMutableClass.methods.find { method -> method.name == "onBackPressed" }
                    ?: throw PatchException("Could not find onBackPressedMethod")
            onCreateMethod = it.mutableMethod
        } ?: throw MainActivityFingerprint.exception
    }

    fun injectInit(
        methods: String,
        descriptor: String
    ) {
        onCreateMethod.apply {
            addInstruction(
                2,
                "invoke-static/range {p0 .. p0}, $UTILS_PATH/$methods;->$descriptor(Landroid/content/Context;)V"
            )
        }
    }
}