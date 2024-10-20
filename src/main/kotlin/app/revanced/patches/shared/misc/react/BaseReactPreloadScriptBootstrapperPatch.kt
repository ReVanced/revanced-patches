package app.revanced.patches.shared.misc.react

import app.revanced.patcher.PatchClass
import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.extensions.InstructionExtensions.getInstructions
import app.revanced.patcher.fingerprint.MethodFingerprint
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.util.proxy.mutableTypes.MutableMethod.Companion.toMutable
import app.revanced.patches.shared.misc.react.fingerprints.LoadScriptFromAssetsFingerprint
import app.revanced.patches.shared.misc.react.fingerprints.LoadScriptFromFileFingerprint
import app.revanced.util.resultOrThrow
import com.android.tools.smali.dexlib2.builder.MutableMethodImplementation
import com.android.tools.smali.dexlib2.immutable.ImmutableMethod

abstract class BaseReactPreloadScriptBootstrapperPatch(
    name: String? = null,
    description: String? = null,
    compatiblePackages: Set<CompatiblePackage>? = null,
    dependencies: Set<PatchClass>? = null,
    use: Boolean = true,
    fingerprints: Set<MethodFingerprint> = emptySet(),
    private val mainActivityOnCreateFingerprintInsertIndexPair: Pair<MethodFingerprint, Int>,
) : BytecodePatch(
    name = name,
    description = description,
    compatiblePackages = compatiblePackages,
    dependencies = dependencies,
    use = use,
    requiresIntegrations = true,
    fingerprints = setOf(
        LoadScriptFromAssetsFingerprint,
        LoadScriptFromFileFingerprint,
        mainActivityOnCreateFingerprintInsertIndexPair.first,
    ) + fingerprints,
) {
    abstract val integrationsClassDescriptor: String

    override fun execute(context: BytecodeContext) {
        val (mainActivityOnCreateFingerprint, insertIndex) = mainActivityOnCreateFingerprintInsertIndexPair
        val loadScriptFromAssetMethod = LoadScriptFromAssetsFingerprint.resultOrThrow().mutableMethod
        val (catalystInstanceImplClassDef, loadScriptFromFileMethod) = LoadScriptFromFileFingerprint.resultOrThrow()
            .let { it.mutableClass to it.mutableMethod }

        // Create preload script on main activity creation.
        mainActivityOnCreateFingerprint.resultOrThrow().mutableMethod.addInstructions(
            insertIndex, // Skip super call.
            "invoke-static { p0 }, " +
                "$integrationsClassDescriptor->hookOnCreate(Landroid/app/Activity;)V",
        )

        // Copy of loadScriptFromFile method acts as a preload script loader.
        catalystInstanceImplClassDef.methods.add(
            ImmutableMethod(
                loadScriptFromFileMethod.definingClass,
                "loadPreloadScriptFromFile",
                loadScriptFromFileMethod.parameters,
                loadScriptFromFileMethod.returnType,
                loadScriptFromFileMethod.accessFlags,
                null,
                null,
                MutableMethodImplementation(4),
            ).toMutable().apply {
                // Copy list as the reference is modified below.
                addInstructions(loadScriptFromFileMethod.getInstructions().toList())
            },
        )

        // Load preload script.
        listOf(loadScriptFromFileMethod, loadScriptFromAssetMethod).forEach { loadScriptMethod ->
            loadScriptMethod.addInstructions(
                0,
                "invoke-static { v0 }, $integrationsClassDescriptor->" +
                    "hookLoadScriptFromFile($catalystInstanceImplClassDef)V",
            )
        }
    }
}
