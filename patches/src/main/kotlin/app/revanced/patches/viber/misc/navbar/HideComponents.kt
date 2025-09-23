package app.revanced.patches.viber.misc.navbar

import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.fingerprint
import app.revanced.patcher.patch.bytecodePatch

@Suppress("unused")
val hideNavbarItemsPatch = bytecodePatch(
    name = "Hide Navbar Items",
    description = "Hides all bottom navbar tabs, except 'Chats', 'Calls' and 'More'.",
) {
    compatibleWith("com.viber.voip"("26.1.2.0"))

    execute {
        val tabClass = tabIdClassFingerprint.classDef

        fingerprint {
            parameters("I", "I")
            returns("Z")
            custom { methodDef, classDef ->
                classDef == tabClass
            }
        }.method.addInstructions(0, """
            # Note: the tab "more" is always present
            const/4 v0, 0x0  # Allow tabId 0 (Chats)
            if-eqz p1, :continue
            
            const/4 v0, 0x1  # Allow tabId 1 (Calls)
            if-eq p1, v0, :continue
            
            const/4 v0, 0x7  # Allow tabId 7 (Calls alternative)
            if-eq p1, v0, :continue
            
            const/4 v0, 0x0
            return v0  # false, do not enable other tabs
            # Proceed with default execution
            :continue
        """
    }
}
