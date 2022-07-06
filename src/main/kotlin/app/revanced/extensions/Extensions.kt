package app.revanced.extensions

import app.revanced.patcher.data.impl.BytecodeData
import app.revanced.patcher.extensions.addInstructions
import app.revanced.patcher.patch.PatchResultError
import app.revanced.patcher.util.proxy.mutableTypes.MutableMethod
import app.revanced.patcher.util.smali.toInstruction
import org.jf.dexlib2.AccessFlags
import org.jf.dexlib2.Opcode
import org.jf.dexlib2.builder.MutableMethodImplementation
import org.jf.dexlib2.builder.instruction.BuilderInstruction11n
import org.jf.dexlib2.builder.instruction.BuilderInstruction11x
import org.jf.dexlib2.builder.instruction.BuilderInstruction21t
import org.jf.dexlib2.builder.instruction.BuilderInstruction35c
import org.jf.dexlib2.immutable.reference.ImmutableMethodReference
import org.w3c.dom.Node

internal fun MutableMethodImplementation.injectHideCall(
    index: Int,
    register: Int
) {
    this.addInstruction(
        index,
        "invoke-static { v$register }, Lapp/revanced/integrations/patches/HideHomeAdsPatch;->HideHomeAds(Landroid/view/View;)V".toInstruction()
    )
}

/**
 * Insert an event hook at the top of the method. If the hook returns true, the event is consumed and the method will return with true
 *
 * the hook method MUST return a boolean and MUST take two parameters, like so:
 *  fun hook(thisRef: Object, eventData: Object): Boolean {}
 *
 * The final injected code will resemble the following logic:
 *  if( YouHook(this, event) ) { return true; }
 *  ...
 *
 * @param hookRef reference to the hook method
 */
internal fun MutableMethod.injectConsumableEventHook(hookRef: ImmutableMethodReference) {
    val isStaticMethod = AccessFlags.STATIC.isSet(this.accessFlags)
    this.implementation?.let { impl ->
        // create label to index 0 to continue to the normal program flow
        val lblContinueNormalFlow = impl.newLabelForIndex(0)

        // define registers
        /** V0 */
        val regV0 = 0

        /** this  */
        val regP0 = impl.registerCount - this.parameters.size - (if (isStaticMethod) 0 else 1)

        /** motionEvent */
        val regP1 = regP0 + 1

        // insert instructions at the start of the method:
        // if( Hook(this, event) ) { return true; }
        impl.addInstructions(
            0, listOf(
                // invoke-static { p0, p1 } <hook>
                BuilderInstruction35c(
                    Opcode.INVOKE_STATIC,
                    2,
                    regP0,
                    regP1,
                    0, 0, 0,
                    hookRef
                ),

                // move-result v0
                BuilderInstruction11x(
                    Opcode.MOVE_RESULT,
                    regV0
                ),

                // if-eqz v0, :continue_normal_flow
                BuilderInstruction21t(
                    Opcode.IF_EQZ,
                    regV0,
                    lblContinueNormalFlow
                ),

                // const/4 v0, 0x1
                BuilderInstruction11n(
                    Opcode.CONST_4,
                    regV0,
                    0x1
                ),

                // return v0
                BuilderInstruction11x(
                    Opcode.RETURN,
                    regV0
                )

                // :continue_normal_flow
            )
        )
    }
}

/**
 * Insert instructions into a named method
 *
 * @param targetClass the name of the class of which the method is a member
 * @param targetMethod the name of the method to insert into
 * @param index index to insert the instructions at. If the index is negative, it is used as an offset to the last method (so -1 inserts at the end of the method)
 * @param instructions the smali instructions to insert (they'll be compiled by MutableMethod.addInstructions)
 */
internal fun BytecodeData.injectIntoNamedMethod(
    targetClass: String,
    targetMethod: String,
    index: Int,
    instructions: String
) {
    var injections = 0
    this.classes.filter { it.type.endsWith("$targetClass;") }.forEach { classDef ->
        this.proxy(classDef).resolve().methods.filter { it.name == targetMethod }.forEach { methodDef ->
            // if index is negative, interpret as an offset from the back
            var insertIndex = index
            if (insertIndex < 0) {
                insertIndex += methodDef.implementation!!.instructions.size
            }

            // insert instructions
            methodDef.addInstructions(insertIndex, instructions)
            injections++
        }
    }

    // fail if nothing was injected
    if (injections <= 0) {
        throw PatchResultError("failed to inject into $targetClass.$targetMethod: no targets were found")
    }
}

internal fun Node.doRecursively(action: (Node) -> Unit) {
    action(this)
    for (i in 0 until this.childNodes.length) this.childNodes.item(i).doRecursively(action)
}

internal fun String.startsWithAny(vararg prefixes: String): Boolean {
    for (prefix in prefixes)
        if (this.startsWith(prefix))
            return true

    return false
}

internal fun String.equalsAny(vararg other: String): Boolean {
    for (_other in other)
        if (this == _other)
            return true

    return false
}
