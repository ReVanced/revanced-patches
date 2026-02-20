package app.revanced.patches.gamehub.network

import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.extensions.InstructionExtensions.replaceInstruction
import app.revanced.patcher.extensions.InstructionExtensions.getInstruction
import app.revanced.patcher.extensions.InstructionExtensions.removeInstruction
import app.revanced.patcher.patch.bytecodePatch
import app.revanced.patches.gamehub.misc.errorhandling.errorHandlingPatch
import app.revanced.patches.gamehub.misc.extension.sharedGamehubExtensionPatch
import app.revanced.patches.gamehub.misc.settings.CONTENT_TYPE_API
import app.revanced.patches.gamehub.misc.settings.addSteamSetting
import app.revanced.patches.gamehub.misc.settings.settingsMenuPatch
import app.revanced.util.getReference
import app.revanced.util.indexOfFirstInstructionOrThrow
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.iface.instruction.OneRegisterInstruction
import com.android.tools.smali.dexlib2.iface.instruction.ReferenceInstruction
import com.android.tools.smali.dexlib2.iface.instruction.formats.Instruction35c
import com.android.tools.smali.dexlib2.iface.reference.FieldReference
import com.android.tools.smali.dexlib2.iface.reference.MethodReference
import com.android.tools.smali.dexlib2.iface.reference.TypeReference

private const val STEAM_EXTENSION = "Lapp/revanced/extension/gamehub/prefs/GameHubPrefs;"

@Suppress("unused")
val apiServerSwitchPatch = bytecodePatch(
    name = "API server switch",
    description = "Allows switching between the official GameHub API and the EmuReady API server.",
) {
    compatibleWith("com.xiaoji.egggame"("5.3.5"))

    dependsOn(sharedGamehubExtensionPatch, errorHandlingPatch, settingsMenuPatch)

    execute {
        addSteamSetting(CONTENT_TYPE_API, "CONTENT_TYPE_API")

        // Patch both NetOkHttpInterceptor classes to add browser-like headers needed by
        // the EmuReady Cloudflare Worker endpoint.
        fun injectCompatibilityHeaders(fingerprint: app.revanced.patcher.Fingerprint) {
            fingerprint.method.apply {
                val newBuilderIndex = indexOfFirstInstructionOrThrow {
                    opcode == Opcode.INVOKE_VIRTUAL &&
                        (this as? ReferenceInstruction)?.reference?.let {
                            it is MethodReference && it.name == "newBuilder" &&
                                it.returnType == "Lokhttp3/Request\$Builder;"
                        } == true
                }
                val builderReg = getInstruction<OneRegisterInstruction>(newBuilderIndex + 1).registerA
                addInstructions(
                    newBuilderIndex + 2,
                    """
                        invoke-static {v$builderReg}, $STEAM_EXTENSION->addCompatibilityHeaders(Ljava/lang/Object;)Ljava/lang/Object;
                        move-result-object v$builderReg
                        check-cast v$builderReg, Lokhttp3/Request${'$'}Builder;
                    """,
                )
            }
        }
        injectCompatibilityHeaders(drakeNetInterceptorFingerprint)
        injectCompatibilityHeaders(wifiuiNetInterceptorFingerprint)

        // Patch EggGameHttpConfig.<clinit>
        // The clinit selects a URL based on environment flags, then jumps to :goto_0 which
        // does sput-object to store the URL in field "b".  All four paths arrive at the sput
        // via "goto :goto_0", so the sput instruction IS :goto_0 (it carries the label).
        // addInstructions would insert before the sput but the label stays on the sput, so
        // every goto still jumps past our code.
        // Fix: replaceInstruction moves :goto_0 to our getEffectiveApiUrl call, then we
        // re-add move-result + sput so all paths go through the URL substitution.
        eggGameHttpConfigFingerprint.method.apply {
            val sputIndex = indexOfFirstInstructionOrThrow {
                opcode == Opcode.SPUT_OBJECT &&
                    (this as? ReferenceInstruction)?.reference?.let {
                        it is FieldReference &&
                            it.name == "b" &&
                            it.definingClass == "Lcom/xj/common/http/EggGameHttpConfig;"
                    } == true
            }
            val urlRegister = getInstruction<OneRegisterInstruction>(sputIndex).registerA

            replaceInstruction(
                sputIndex,
                "invoke-static {v$urlRegister}, $STEAM_EXTENSION->getEffectiveApiUrl(Ljava/lang/String;)Ljava/lang/String;",
            )
            addInstructions(
                sputIndex + 1,
                """
                    move-result-object v$urlRegister
                    sput-object v$urlRegister, Lcom/xj/common/http/EggGameHttpConfig;->b:Ljava/lang/String;
                """,
            )
        }

        // GsonConverter — on the catch-all path (:goto_4) that would throw ConvertException,
        // return null instead so JSON parse failures from the alternative API return null.
        //
        // The :goto_4 block ends with `throw v0`. Immediately after it (dead code) is
        // `return-object v5` at :cond_5/:goto_5, reached from the null-body path above.
        // That return-object uses v5, which the ConvertException constructor arguments
        // within the try block set to PositiveByteConstant (const/16 v5, 0xc).
        // ART's verifier merges the types across all predecessors of return-object v5:
        // the null-body path gives Zero but the :goto_4 path (without our fix) would give
        // PositiveByteConstant, causing a VerifyError.
        //
        // Fix: replace new-instance with `const/4 v5, 0x0` (writing to the SAME register
        // as return-object v5), then remove the intervening instructions. The verifier now
        // sees v5 = Zero on both paths → valid reference return.
        //
        // IMPORTANT: use replaceInstruction (not removeInstruction + addInstructions) so the
        // :goto_4 catch-handler label stays on the const/4 instruction, not the return-object.
        gsonConverterFingerprint.method.apply {
            val newInstanceIndex = indexOfFirstInstructionOrThrow {
                opcode == Opcode.NEW_INSTANCE &&
                    getReference<TypeReference>()?.type ==
                    "Lcom/drake/net/exception/ConvertException;"
            }

            val throwIndex = indexOfFirstInstructionOrThrow(newInstanceIndex) {
                opcode == Opcode.THROW
            }

            // The instruction at throwIndex+1 is `return-object v5` (dead code after throw).
            // Use the same register in our const/4 so the verifier sees v5=Zero on the
            // catch path (eliminating the PositiveByteConstant from the constructor args).
            val returnReg = getInstruction<OneRegisterInstruction>(throwIndex + 1).registerA

            // Remove from throw down to new-instance+1, leaving new-instance in place.
            for (i in throwIndex downTo newInstanceIndex + 1) {
                removeInstruction(i)
            }

            // Replace new-instance with const/4 null on returnReg, preserving :goto_4 label.
            // The now-adjacent return-object v5 acts as our null return.
            replaceInstruction(newInstanceIndex, "const/4 v$returnReg, 0x0")
        }

        // Patch wifiui HttpConfig.b(Context)
        // The method has a hardcoded const-string URL passed directly as the first argument to NetConfig.l().
        // Smali: invoke-virtual {p0, v1, p1, v0}, NetConfig;->l(String;Context;Function1;)V
        // In Instruction35c layout, registerD holds the first method argument (the URL string, v1).
        // We intercept just before the call to optionally replace the URL register.
        wifiuiHttpConfigFingerprint.method.apply {
            val netConfigCallIndex = indexOfFirstInstructionOrThrow {
                opcode == Opcode.INVOKE_VIRTUAL &&
                    (this as? ReferenceInstruction)?.reference?.let {
                        it is MethodReference && it.name == "l" &&
                            it.definingClass == "Lcom/xj/adb/wifiui/net/NetConfig;"
                    } == true
            }
            // registerD is the first method argument (URL string) in invoke-virtual {instance, v1, ...}
            val urlRegister = getInstruction<Instruction35c>(netConfigCallIndex).registerD

            addInstructions(
                netConfigCallIndex,
                """
                    invoke-static {v$urlRegister}, $STEAM_EXTENSION->getEffectiveApiUrl(Ljava/lang/String;)Ljava/lang/String;
                    move-result-object v$urlRegister
                """,
            )
        }
    }
}
