package app.revanced.patches.gamehub.misc.push

import app.revanced.patcher.extensions.InstructionExtensions.replaceInstruction
import app.revanced.patcher.patch.bytecodePatch
import app.revanced.patcher.patch.resourcePatch
import app.revanced.patches.gamehub.misc.stability.appNullSafetyPatch
import app.revanced.util.asSequence
import app.revanced.util.returnEarly
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.iface.instruction.NarrowLiteralInstruction

private val disablePushManifestPatch = resourcePatch {
    execute {
        document("AndroidManifest.xml").use { dom ->
            fun removeByName(vararg tags: String, predicate: (String) -> Boolean) {
                tags.flatMap { tag ->
                    dom.getElementsByTagName(tag).asSequence()
                        .filter { predicate(it.attributes.getNamedItem("android:name")?.nodeValue ?: "") }
                        .toList()
                }.forEach { it.parentNode?.removeChild(it) }
            }

            // Remove JPUSH_MESSAGE custom permission declarations (children of <manifest>)
            removeByName("permission", "uses-permission") { it.contains("JPUSH_MESSAGE") }

            // Remove JPush SDK components (service/receiver/activity/provider)
            removeByName("service", "receiver", "activity", "provider") {
                it.startsWith("cn.jpush.") ||
                    it.startsWith("com.xj.push.jiguang.") ||
                    it.startsWith("cn.android.service.JTransit")
            }

            // Remove JPush/JCore meta-data keys
            removeByName("meta-data") {
                it == "JPUSH_CHANNEL" || it == "JPUSH_APPKEY" || it == "JCORE_VT"
            }

            // Remove notification permissions (no longer needed with push disabled).
            removeByName("uses-permission") {
                it == "android.permission.POST_NOTIFICATIONS" ||
                    it == "android.permission.NOTIFICATION_SERVICE"
            }
        }
    }
}

@Suppress("unused")
val disablePushPatch = bytecodePatch(
    name = "Disable push notifications",
    description = "Disables JPush notification service initialization and defaults notification settings to off.",
) {
    compatibleWith("com.xiaoji.egggame"("5.3.5"))

    dependsOn(appNullSafetyPatch, disablePushManifestPatch)

    execute {
        pushAppFingerprint.method.returnEarly()
        pushAppModuleFingerprint.method.returnEarly()
        // Suppress the "Turn on message notifications" rationale dialog shown on first launch.
        permissionDialogFingerprint.method.returnEarly()
        // Prevent "Push Notification" settings item from opening system notification settings.
        openNotificationSettingsFingerprint.method.returnEarly()

        // Change notification setting defaults from OPEN (1) to CLOSE (2) so that
        // when the server is unreachable the UI shows all toggles as disabled.
        notificationSettingDefaultsFingerprint.method.apply {
            val constIdx = implementation!!.instructions.indexOfFirst {
                it.opcode == Opcode.CONST_4 &&
                    (it as NarrowLiteralInstruction).narrowLiteral == 1
            }
            replaceInstruction(constIdx, "const/4 v0, 0x2")
        }
    }
}
