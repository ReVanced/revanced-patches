package app.revanced.patches.gamehub.misc.settings

import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.extensions.InstructionExtensions.addInstructionsWithLabels
import app.revanced.patcher.extensions.InstructionExtensions.getInstruction
import app.revanced.patcher.extensions.InstructionExtensions.replaceInstruction
import app.revanced.patcher.patch.BytecodePatchContext
import app.revanced.patcher.patch.bytecodePatch
import app.revanced.patcher.util.proxy.mutableTypes.MutableClass
import app.revanced.patcher.util.proxy.mutableTypes.MutableField.Companion.toMutable
import app.revanced.util.getReference
import app.revanced.util.indexOfFirstInstructionOrThrow
import app.revanced.util.indexOfFirstInstructionReversedOrThrow
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.iface.instruction.OneRegisterInstruction
import com.android.tools.smali.dexlib2.iface.instruction.ReferenceInstruction
import com.android.tools.smali.dexlib2.iface.reference.MethodReference
import com.android.tools.smali.dexlib2.iface.reference.StringReference
import com.android.tools.smali.dexlib2.immutable.ImmutableField
import com.android.tools.smali.dexlib2.immutable.value.ImmutableIntEncodedValue

private const val ENTITY_CLASS = "Lcom/xj/landscape/launcher/data/model/entity/SettingItemEntity;"
private const val EXTENSION = "Lapp/revanced/extension/gamehub/prefs/GameHubPrefs;"

// Content-type constants — must match GameHubPrefs.CONTENT_TYPE_* values.
internal const val CONTENT_TYPE_SD_CARD_STORAGE = 0x18
internal const val CONTENT_TYPE_API = 0x1a

private lateinit var entityMutableClass: MutableClass

// Insertion point in SettingItemViewModel.l() after the Language item's add() call.
// Incremented by 9 (instructions per entry) with each addSteamSetting() call so entries
// appear in registration order.
private var viewModelInsertionIndex = 0

internal val settingsMenuPatch = bytecodePatch {
    execute {
        entityMutableClass = proxy(settingItemEntityFingerprint.classDef).mutableClass

        viewModelInsertionIndex = settingItemViewModelFingerprint.method
            .indexOfFirstInstructionOrThrow {
                opcode == Opcode.INVOKE_INTERFACE &&
                    (this as? ReferenceInstruction)?.reference?.let {
                        it is MethodReference &&
                            it.definingClass == "Ljava/util/List;" &&
                            it.name == "add"
                    } == true
            } + 1

        // Patch getContentName() to return titles for our custom settings items.
        //
        // The method ends with a bare const-string "" fallthrough for unknown types.
        // We REPLACE that const-string "" instruction (preserving the :cond_15 label so
        // existing if-ne branches still land on our code), then insert result-dispatch
        // logic immediately after.  All labels (:has_custom_name) are internal to the
        // injected block, avoiding any ExternalLabel chaining issues.
        settingItemEntityTitleFingerprint.method.apply {
            val fallthroughIndex = indexOfFirstInstructionReversedOrThrow {
                opcode == Opcode.CONST_STRING && getReference<StringReference>()?.string == ""
            }

            // Replace const-string "" so :cond_15 now points to our extension call.
            replaceInstruction(
                fallthroughIndex,
                "invoke-static {p0}, $EXTENSION->getCustomSettingName(I)Ljava/lang/String;",
            )

            // Insert result-dispatch:
            //   if non-null → return the custom name
            //   if null     → fall through to const-string "" + return-object
            // :has_custom_name is a forward branch within this block (internal label).
            addInstructionsWithLabels(
                fallthroughIndex + 1,
                """
                    move-result-object v0
                    if-nez v0, :has_custom_name
                    const-string p0, ""
                    return-object p0
                    :has_custom_name
                    return-object v0
                """,
            )
        }

        // Intercept toggle events for all registered settings items.
        //
        // w() reads getSwitch() into p2, then computes v0 = !p2 (the proposed new state).
        // It then calls CommFocusSwitchBtn.b(v0, true) to apply the visual state — but only
        // for non-notification types (guarded by if-nez v1, :cond_0 after isNotificationContentType).
        //
        // We inject handleSettingToggle(contentType, proposedState) just before that if-nez,
        // where p0=binding and p1=entity are still live.  The method returns the ACTUAL state
        // to use (false on failure, e.g. SD card not found), overwriting v0 so that b() sets
        // the correct visual state immediately — no reopen needed.
        settingSwitchHolderFingerprint.method.apply {
            // xor-int/lit8 v0, p2, 0x1  computes v0 = proposed state.  After it:
            //   +1  invoke isNotificationContentType
            //   +2  move-result v1
            //   +3  const/4 v2, 0x1
            //   +4  if-nez v1, :cond_0  ← insert our 4-instruction block here (no label)
            val xorIndex = indexOfFirstInstructionOrThrow { opcode == Opcode.XOR_INT_LIT8 }
            addInstructions(
                xorIndex + 4,
                """
                    invoke-virtual {p1}, Lcom/xj/landscape/launcher/data/model/entity/SettingItemEntity;->getContentType()I
                    move-result v3
                    invoke-static {v3, v0}, $EXTENSION->handleSettingToggle(IZ)Z
                    move-result v0
                """,
            )
        }

        // Patch the initial visual switch state in SettingSwitchHolder.u() (the bind method).
        //
        // For non-notification types, u() reads the switch state from CloudGameSettingDataHelper.j()
        // rather than entity.switchValue. For our custom content types this always returns false.
        // We inject getInitialSwitchValue(contentType, defaultValue) after the helper call to
        // substitute our persisted preference value for 0x18/0x1a, pass-through for everything else.
        settingSwitchHolderBindFingerprint.method.apply {
            val cloudHelperIndex = indexOfFirstInstructionOrThrow {
                opcode == Opcode.INVOKE_STATIC_RANGE &&
                    (this as? ReferenceInstruction)?.reference?.let {
                        it is MethodReference &&
                            it.definingClass == "Lcom/xj/cloud/ui/setting/CloudGameSettingDataHelper;" &&
                            it.name == "j"
                    } == true
            }
            // Reverse-search for getContentType() which precedes the CloudGameSettingDataHelper call.
            val getContentTypeIndex = indexOfFirstInstructionReversedOrThrow {
                opcode == Opcode.INVOKE_VIRTUAL &&
                    (this as? ReferenceInstruction)?.reference?.let {
                        it is MethodReference && it.name == "getContentType"
                    } == true
            }
            val contentTypeReg = getInstruction<OneRegisterInstruction>(getContentTypeIndex + 1).registerA
            val switchValueReg = getInstruction<OneRegisterInstruction>(cloudHelperIndex + 1).registerA

            // Insert after move-result (cloudHelperIndex+1). The :goto_1 label sits on the
            // CommFocusSwitchBtn.b() call below, so the notification path's "goto :goto_1" is
            // unaffected; only the non-notification fall-through passes through our override.
            addInstructions(
                cloudHelperIndex + 2,
                """
                    invoke-static {v$contentTypeReg, v$switchValueReg}, $EXTENSION->getInitialSwitchValue(IZ)Z
                    move-result v$switchValueReg
                """,
            )
        }
    }
}

/**
 * Registers a new toggle item in the Steam settings menu.
 *
 * Must be called from within a patch's execute block that depends on [settingsMenuPatch].
 * Performs two injections:
 *  1. Adds a named constant field to [SettingItemEntity].
 *  2. Appends a TYPE_SWITCH entry to [SettingItemViewModel.l()].
 *
 * Title display is handled centrally by SteamStoragePreference.getCustomSettingName()
 * which is called from getContentName() via settingsMenuPatch.
 *
 * @param contentType  The integer content-type constant (e.g. 0x18).
 * @param fieldName    The static field name to add to SettingItemEntity (e.g. "CONTENT_TYPE_SD_CARD_STORAGE").
 */
context(BytecodePatchContext)
internal fun addSteamSetting(contentType: Int, fieldName: String) {
    val hexType = "0x${contentType.toString(16)}"

    // 1. Add a named constant to SettingItemEntity.
    entityMutableClass.staticFields.add(
        ImmutableField(
            ENTITY_CLASS,
            fieldName,
            "I",
            AccessFlags.PUBLIC.value or AccessFlags.STATIC.value or AccessFlags.FINAL.value,
            ImmutableIntEncodedValue(contentType),
            emptySet(),
            null,
        ).toMutable(),
    )

    // 2. Inject a TYPE_SWITCH (5) list entry into SettingItemViewModel.l().
    //    Constructor: <init>(IILandroid/util/SparseArray;ZILkotlin/jvm/internal/DefaultConstructorMarker;)V
    //    Registers: v0=this, v1=type(5), v2=contentType, v3=sparseArray(null),
    //               v4=Z(false), v5=defaultBitMask(0xc), v6=marker(null).
    //    Each block is exactly 9 instructions; viewModelInsertionIndex is advanced accordingly.
    settingItemViewModelFingerprint.method.addInstructions(
        viewModelInsertionIndex,
        """
            new-instance v0, $ENTITY_CLASS
            const/4 v1, 0x5
            const/16 v2, $hexType
            invoke-static {v2}, $EXTENSION->isSettingEnabled(I)Z
            move-result v4
            const/4 v3, 0x0
            const/16 v5, 0x4
            const/4 v6, 0x0
            invoke-direct/range {v0 .. v6}, $ENTITY_CLASS-><init>(IILandroid/util/SparseArray;ZILkotlin/jvm/internal/DefaultConstructorMarker;)V
            invoke-interface {p0, v0}, Ljava/util/List;->add(Ljava/lang/Object;)Z
        """,
    )
    viewModelInsertionIndex += 10
}
