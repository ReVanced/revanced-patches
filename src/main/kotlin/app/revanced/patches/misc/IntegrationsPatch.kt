package app.revanced.patches.misc

import app.revanced.patcher.cache.Cache
import app.revanced.patcher.extensions.AccessFlagExtensions.Companion.or
import app.revanced.patcher.extensions.addInstructions
import app.revanced.patcher.patch.Patch
import app.revanced.patcher.patch.PatchMetadata
import app.revanced.patcher.patch.PatchResult
import app.revanced.patcher.patch.PatchResultSuccess
import app.revanced.patcher.proxy.mutableTypes.MutableMethod.Companion.toMutable
import app.revanced.patcher.smali.asInstructions
import org.jf.dexlib2.AccessFlags
import org.jf.dexlib2.immutable.ImmutableMethod
import org.jf.dexlib2.immutable.ImmutableMethodImplementation

class IntegrationsPatch : Patch(
    PatchMetadata(
        "integrations",
        "TODO",
        "TODO"
    )
) {
    override fun execute(cache: Cache): PatchResult {
        val map = cache.methodMap["integrations-patch"]
        val implementation = map.method.implementation!!
        val count = implementation.registerCount - 1

        implementation.addInstructions(
            map.scanData.endIndex,
            """
                  invoke-static {v$count}, Lpl/jakubweg/StringRef;->setContext(Landroid/content/Context;)V
                  sput-object v$count, Lapp/revanced/integrations/Globals;->context:Landroid/content/Context;
            """.trimIndent().asInstructions()
        )

        val classDef = map.definingClassProxy.resolve()
        classDef.methods.add(
            ImmutableMethod(
                classDef.type,
                "getAppContext",
                null,
                "Landroid/content/Context;",
                AccessFlags.PUBLIC or AccessFlags.STATIC,
                null,
                null,
                ImmutableMethodImplementation(
                    1,
                    """
                        invoke-static { }, Lapp/revanced/integrations/Globals;->getAppContext()Landroid/content/Context;
                        move-result-object v0
                        return-object v0
                    """.trimIndent().asInstructions(),
                    null,
                    null
                )
            ).toMutable()
        )
        return PatchResultSuccess()
    }
}