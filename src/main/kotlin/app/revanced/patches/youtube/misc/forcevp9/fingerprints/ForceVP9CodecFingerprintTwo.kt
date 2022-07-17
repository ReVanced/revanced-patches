package app.revanced.patches.youtube.misc.forcevp9.fingerprints

import app.revanced.patcher.annotation.Name
import app.revanced.patcher.annotation.Version
import app.revanced.patcher.extensions.or
import app.revanced.patcher.fingerprint.method.annotation.DirectPatternScanMethod
import app.revanced.patcher.fingerprint.method.annotation.MatchingMethod
import app.revanced.patcher.fingerprint.method.impl.MethodFingerprint
import app.revanced.patches.youtube.misc.forcevp9.annotations.ForceVP9Compatibility
import org.jf.dexlib2.AccessFlags
import org.jf.dexlib2.Opcode

@Name("force-vp9-codec-fingerprint-two")
@MatchingMethod(
    "Lpzs;", "aO"
)
@DirectPatternScanMethod
@ForceVP9Compatibility
@Version("0.0.1")
object ForceVP9CodecFingerprintTwo : MethodFingerprint(
    "Z", AccessFlags.PUBLIC or AccessFlags.STATIC, listOf("I"), listOf(
        Opcode.INVOKE_STATIC, Opcode.MOVE_RESULT_OBJECT, Opcode.CONST_4
    ), null, null
)

/*
public static boolean aO(int i) {
    Pair aG = aG();
    return (aG == null ? 0 : Math.min(((Integer) aG.first).intValue(), ((Integer) aG.second).intValue())) >= i;
    //replace line with: return Lapp/revanced/integrations/patches/ForceCodecPatch/shouldForceVP9();
}

becomes:
public static boolean aO(int i) {
    return Lapp/revanced/integrations/patches/ForceCodecPatch/shouldForceVP9();
}

.method public static aO(I)Z
    invoke-static {}, Lpzs;->aG()Landroid/util/Pair;
    move-result-object v0
    const/4 v1, 0x0
    if-nez v0, :cond_9
    const/4 v0, 0x0
    goto :goto_1d
    :cond_9
    iget-object v2, v0, Landroid/util/Pair;->first:Ljava/lang/Object;
    check-cast v2, Ljava/lang/Integer;
    invoke-virtual {v2}, Ljava/lang/Integer;->intValue()I
    move-result v2
    iget-object v0, v0, Landroid/util/Pair;->second:Ljava/lang/Object;
    check-cast v0, Ljava/lang/Integer;
    invoke-virtual {v0}, Ljava/lang/Integer;->intValue()I
    move-result v0
    invoke-static {v2, v0}, Ljava/lang/Math;->min(II)I
    move-result v0
    :goto_1d
    if-lt v0, p0, :cond_21
    const/4 p0, 0x1
    return p0
    :cond_21
    return v1
.end method


becomes:
.method public static aO(I)Z
    invoke-static {}, Lapp/revanced/integrations/patches/ForceCodecPatch;->shouldForceVP9()Z
    move-result v0
    return v0
.end method


 */