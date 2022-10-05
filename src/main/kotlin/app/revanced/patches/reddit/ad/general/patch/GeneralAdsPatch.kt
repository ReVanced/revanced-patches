package app.revanced.patches.reddit.ad.general.patch

import app.revanced.patcher.annotation.Description
import app.revanced.patcher.annotation.Name
import app.revanced.patcher.annotation.Version
import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.PatchResult
import app.revanced.patcher.patch.PatchResultSuccess
import app.revanced.patcher.patch.annotations.Patch
import app.revanced.patches.reddit.ad.general.annotations.GeneralAdsCompatibility
import org.jf.dexlib2.Opcode
import org.jf.dexlib2.builder.instruction.BuilderInstruction21c
import org.jf.dexlib2.iface.instruction.ReferenceInstruction
import org.jf.dexlib2.iface.reference.StringReference
import org.jf.dexlib2.immutable.reference.ImmutableStringReference

@Patch
@Name("general-reddit-ads")
@Description("Removes general ads from the Reddit frontpage and subreddits.")
@GeneralAdsCompatibility
@Version("0.0.1")
class GeneralAdsPatch : BytecodePatch() {
    override fun execute(context: BytecodeContext): PatchResult {
        context.classes.forEach { classDef ->
            classDef.methods.forEach methodLoop@{ method ->
                val implementation = method.implementation ?: return@methodLoop

                implementation.instructions.forEachIndexed { i, instruction ->
                    if (instruction.opcode != Opcode.CONST_STRING) return@forEachIndexed
                    if (((instruction as ReferenceInstruction).reference as StringReference).string != "AdPost") return@forEachIndexed

                    val proxiedClass = context.proxy(classDef).mutableClass

                    val proxiedImplementation = proxiedClass.methods.first {
                        it.name == method.name && it.parameterTypes.containsAll(method.parameterTypes)
                    }.implementation!!

                    var newString = "AdPost1"
                    if (proxiedImplementation.instructions[i - 1].opcode == Opcode.CONST_STRING) {
                        newString = "SubredditPost"
                    }
                    proxiedImplementation.replaceInstruction(
                        i, BuilderInstruction21c(
                            Opcode.CONST_STRING, (proxiedImplementation.instructions[i] as BuilderInstruction21c).registerA, ImmutableStringReference(newString)
                        )
                    )
                }
            }
        }

        return PatchResultSuccess()
    }
}
