package app.revanced.patches.spotify.layout.hide.createbutton

import app.revanced.patcher.fingerprint
import app.revanced.util.getReference
import app.revanced.util.indexOfFirstInstruction
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.iface.reference.MethodReference

internal val navigationBarItemSetClassFingerprint by fingerprint {
    strings("NavigationBarItemSet(")
}

internal val navigationBarItemSetConstructorFingerprint by fingerprint {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.CONSTRUCTOR)
    // Make sure the method checks whether navigation bar items are null before adding them.
    // If this is not true, then we cannot patch the method and potentially transform the parameters into null.
    opcodes(Opcode.IF_EQZ, Opcode.INVOKE_VIRTUAL)
    custom { method, _ ->
        method.indexOfFirstInstruction {
            getReference<MethodReference>()?.name == "add"
        } >= 0
    }
}

internal val oldNavigationBarAddItemFingerprint by fingerprint {
    strings("Bottom navigation tabs exceeds maximum of 5 tabs")
}
