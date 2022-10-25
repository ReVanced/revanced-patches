package app.revanced.patches.youtube.layout.hidecrowdfundingbox.fingerprints

import app.revanced.patcher.annotation.Name
import app.revanced.patcher.annotation.Version
import app.revanced.patcher.extensions.or
import app.revanced.patcher.fingerprint.method.annotation.FuzzyPatternScanMethod
import app.revanced.patcher.fingerprint.method.impl.MethodFingerprint
import app.revanced.patches.youtube.layout.hidecrowdfundingbox.annotations.CrowdfundingBoxCompatibility
import org.jf.dexlib2.AccessFlags
import org.jf.dexlib2.Opcode

@Name("crowdfunding-box-view-fingerprint")
@FuzzyPatternScanMethod(3)
@CrowdfundingBoxCompatibility
@Version("0.0.1")
object CrowdfundingBoxFingerprint : MethodFingerprint(
    "V",
    AccessFlags.PUBLIC or AccessFlags.CONSTRUCTOR,
    listOf("L", "L", "L", "L", "L", "L", "L", "[B", "[B"),
    listOf(
        Opcode.MOVE_OBJECT,
        Opcode.MOVE_OBJECT_FROM16,
        Opcode.INVOKE_DIRECT,
        Opcode.MOVE_OBJECT_FROM16,
        Opcode.IPUT_OBJECT,
        Opcode.MOVE_OBJECT_FROM16,
        Opcode.IPUT_OBJECT,
        Opcode.IPUT_OBJECT,
        Opcode.INVOKE_STATIC,
        Opcode.MOVE_RESULT_OBJECT,
        Opcode.CONST_4,
        Opcode.CONST_4,
        Opcode.CONST,
        Opcode.INVOKE_VIRTUAL,
        Opcode.MOVE_RESULT_OBJECT,
    )
)