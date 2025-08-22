package app.revanced.patches.reddit.customclients.sync.syncforreddit.fix.redgifs

import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.extensions.InstructionExtensions.getInstruction
import app.revanced.patcher.extensions.InstructionExtensions.getInstructionOrNull
import app.revanced.patcher.patch.BytecodePatchContext
import app.revanced.patcher.patch.PatchException
import app.revanced.patcher.patch.bytecodePatch
import app.revanced.patches.reddit.customclients.sync.syncforreddit.extension.sharedExtensionPatch
import app.revanced.util.getReference
import com.android.tools.smali.dexlib2.iface.ClassDef
import com.android.tools.smali.dexlib2.iface.Field
import com.android.tools.smali.dexlib2.iface.reference.FieldReference

private const val EXTENSION_CLASS_DESCRIPTOR =
    "Lapp/revanced/extension/syncforreddit/FixRedgifsPatch;"

private const val RESPONSE_LISTENER_DESCRIPTOR = "Lcom/android/volley/Response\$Listener;"
private const val FETCH_VIDEO_URL_METHOD = "fetchVideoUrl(Ljava/lang/String;Z$RESPONSE_LISTENER_DESCRIPTOR)V"

context(BytecodePatchContext)
private fun ClassDef.inspectRequestDataClass(): RequestDataClassInfo? {
    val stringFieldsWithSetter = methods
        .mapNotNull { stringSetterFingerprint.matchOrNull(it) }
        .mapNotNull{ it.method.getInstructionOrNull(0)?.getReference<FieldReference>() }
        .map { it.name }

    val allStringFields = instanceFields.filter { it.type == "Ljava/lang/String;" }

    // url field is the only field without a setter
    val urlField = allStringFields.firstOrNull { it.name !in stringFieldsWithSetter } ?: return null
    val isHdField = instanceFields.firstOrNull { it.type == "Z" } ?: return null
    val listenerField = instanceFields.firstOrNull { it.type == RESPONSE_LISTENER_DESCRIPTOR } ?: return null

    return RequestDataClassInfo(urlField, isHdField, listenerField)
}

data class RequestDataClassInfo(val urlField: Field, val isHdField: Field, val listenerField: Field)

@Suppress("unused")
val fixRedgifsPatch = bytecodePatch(
    name = "Fix Redgifs",
    description = "Fixes Redgifs playback.",
) {
    dependsOn(sharedExtensionPatch)

    compatibleWith(
        "com.laurencedawson.reddit_sync",
        "com.laurencedawson.reddit_sync.pro",
        "com.laurencedawson.reddit_sync.dev",
    )

    execute {
        deliverRegifsOauthResponseFingerprint.method.apply {
            val requestField = getInstruction(0).getReference<FieldReference>()
                ?: throw PatchException("Unexpected instruction!")

            val info = classBy { it.type == requestField.type }
                ?.immutableClass
                ?.inspectRequestDataClass() ?: throw PatchException("Failed to find info!")

            addInstructions(
                0,
                """
                iget-object p1, p0, $requestField
                iget-object p1, p1, ${info.urlField}
                iget-object v0, p0, $requestField
                iget-boolean v0, v0, ${info.isHdField}
                iget-object v1, p0, $requestField
                iget-object v1, v1, ${info.listenerField}

                invoke-static { p1, v0, v1 }, $EXTENSION_CLASS_DESCRIPTOR->$FETCH_VIDEO_URL_METHOD
                return-void
            """
            )
        }
    }
}
