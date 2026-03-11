@file:Suppress("SpellCheckingInspection")

package app.revanced.patches.shared.misc.litho.context


import app.revanced.com.android.tools.smali.dexlib2.mutable.MutableClassDef
import app.revanced.com.android.tools.smali.dexlib2.mutable.MutableMethod.Companion.toMutable
import app.revanced.patcher.after
import app.revanced.patcher.allOf
import app.revanced.patcher.classDef
import app.revanced.patcher.extensions.addInstructions
import app.revanced.patcher.field
import app.revanced.patcher.firstClassDef
import app.revanced.patcher.firstMethodDeclaratively
import app.revanced.patcher.instructions
import app.revanced.patcher.invoke
import app.revanced.patcher.parameterTypes
import app.revanced.patcher.patch.bytecodePatch
import app.revanced.patcher.returnType
import app.revanced.patches.shared.misc.extension.sharedExtensionPatch
import app.revanced.util.findFieldFromToString
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.builder.MutableMethodImplementation
import com.android.tools.smali.dexlib2.immutable.ImmutableMethod

internal const val EXTENSION_CONTEXT_INTERFACE =
    $$"Lapp/revanced/extension/shared/ConversionContext$ContextInterface;"

internal lateinit var conversionContextClassDef: MutableClassDef

val conversionContextPatch = bytecodePatch(
    description = "Hooks the method to use the conversion context in an extension.",
) {
    dependsOn(sharedExtensionPatch())

    apply {
        conversionContextClassDef = conversionContextToStringMethod.classDef

        val identifierField = conversionContextToStringMethod
            .findFieldFromToString(IDENTIFIER_PROPERTY)
        val stringBuilderField = conversionContextClassDef
            .fields.single { field -> field.type == "Ljava/lang/StringBuilder;" }

        // The conversionContext class can be used as is in most versions.
        if (conversionContextClassDef.superclass == "Ljava/lang/Object;") {
            arrayOf(
                identifierField,
                stringBuilderField
            ).map {

            }
            conversionContextClassDef.apply {
                // Add interface and helper methods to allow extension code to call obfuscated methods.
                interfaces += EXTENSION_CONTEXT_INTERFACE

                arrayOf(
                    Triple(
                        "patch_getIdentifier",
                        "Ljava/lang/String;",
                        identifierField
                    ),
                    Triple(
                        "patch_getPathBuilder",
                        "Ljava/lang/StringBuilder;",
                        stringBuilderField
                    )
                ).forEach { (interfaceMethodName, interfaceMethodReturnType, classFieldReference) ->
                    ImmutableMethod(
                        type,
                        interfaceMethodName,
                        listOf(),
                        interfaceMethodReturnType,
                        AccessFlags.PUBLIC.value or AccessFlags.FINAL.value,
                        null,
                        null,
                        MutableMethodImplementation(2),
                    ).toMutable().apply {
                        addInstructions(
                            0,
                            """
                                iget-object v0, p0, $classFieldReference
                                return-object v0
                            """
                        )
                    }.let(methods::add)

                }
            }
        } else {
            // In some special versions, such as YouTube 20.41, it inherits from an abstract class,
            // in which case a helper method is added to the abstract class.

            // Since fields cannot be accessed directly in an abstract class, abstract methods are linked.
            val stringBuilderMethodName = conversionContextClassDef.firstMethodDeclaratively {
                parameterTypes()
                returnType("Ljava/lang/String;")
                instructions(
                    allOf(Opcode.IGET_OBJECT(), field { this == identifierField }),
                    after(Opcode.RETURN_OBJECT()),
                )
            }.name

            val identifierMethodName = conversionContextClassDef.firstMethodDeclaratively {
                parameterTypes()
                returnType("Ljava/lang/StringBuilder;")
                instructions(
                    allOf(Opcode.IGET_OBJECT(), field { this == stringBuilderField }),
                    after(Opcode.RETURN_OBJECT()),
                )
            }.name

            conversionContextClassDef = firstClassDef(conversionContextClassDef.superclass!!)

            conversionContextClassDef.apply {
                // Add interface and helper methods to allow extension code to call obfuscated methods.
                interfaces += EXTENSION_CONTEXT_INTERFACE

                arrayOf(
                    Triple(
                        "patch_getIdentifier",
                        "Ljava/lang/String;",
                        identifierMethodName
                    ),
                    Triple(
                        "patch_getPathBuilder",
                        "Ljava/lang/StringBuilder;",
                        stringBuilderMethodName
                    )
                ).forEach { (interfaceMethodName, interfaceMethodReturnType, classMethodName) ->
                    ImmutableMethod(
                        type,
                        interfaceMethodName,
                        listOf(),
                        interfaceMethodReturnType,
                        AccessFlags.PUBLIC.value or AccessFlags.FINAL.value,
                        null,
                        null,
                        MutableMethodImplementation(2),
                    ).toMutable().apply {
                        addInstructions(
                            0,
                            """
                                invoke-virtual {p0}, $type->$classMethodName()$interfaceMethodReturnType
                                move-result-object v0
                                return-object v0
                            """
                        )
                    }.let(methods::add)
                }
            }
        }
    }
}