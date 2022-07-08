package app.revanced.patches.youtube.layout.widesearchbar.patch

import app.revanced.patcher.annotation.Description
import app.revanced.patcher.annotation.Name
import app.revanced.patcher.annotation.Version
import app.revanced.patcher.data.impl.BytecodeData
import app.revanced.patcher.data.impl.toMethodWalker
import app.revanced.patcher.extensions.addInstructions
import app.revanced.patcher.fingerprint.method.utils.MethodFingerprintUtils.resolve
import app.revanced.patcher.patch.PatchResult
import app.revanced.patcher.patch.PatchResultSuccess
import app.revanced.patcher.patch.annotations.Dependencies
import app.revanced.patcher.patch.annotations.Patch
import app.revanced.patcher.patch.impl.BytecodePatch
import app.revanced.patcher.util.proxy.mutableTypes.MutableMethod
import app.revanced.patches.youtube.layout.widesearchbar.annotations.WideSearchbarCompatibility
import app.revanced.patches.youtube.layout.widesearchbar.fingerprints.methodone.WideSearchbarOneFingerprint
import app.revanced.patches.youtube.layout.widesearchbar.fingerprints.methodone.WideSearchbarOneParentFingerprint
import app.revanced.patches.youtube.layout.widesearchbar.fingerprints.methodtwo.WideSearchbarTwoFingerprint
import app.revanced.patches.youtube.layout.widesearchbar.fingerprints.methodtwo.WideSearchbarTwoParentFingerprint
import app.revanced.patches.youtube.misc.integrations.patch.IntegrationsPatch

@Patch(include = false)
@Dependencies(dependencies = [IntegrationsPatch::class])
@Name("enable-wide-searchbar")
@Description("Replaces the search icon with a wide search bar. This will hide the YouTube logo when active.")
@WideSearchbarCompatibility
@Version("0.0.1")
class WideSearchbarPatch : BytecodePatch(
    listOf(
        WideSearchbarOneParentFingerprint, WideSearchbarTwoParentFingerprint
    )
) {
    override fun execute(data: BytecodeData): PatchResult {
        WideSearchbarOneFingerprint.resolve(data, WideSearchbarOneParentFingerprint.result!!.classDef)
        WideSearchbarTwoFingerprint.resolve(data, WideSearchbarTwoParentFingerprint.result!!.classDef)

        val resultOne = WideSearchbarOneFingerprint.result

        //This should be the method aF in class fbn
        val targetMethodOne =
            data.toMethodWalker(resultOne!!.method).nextMethod(resultOne!!.patternScanResult!!.endIndex, true).getMethod() as MutableMethod

        //Since both methods have the same smali code, inject instructions using a method.
        addInstructions(targetMethodOne)

        val resultTwo = WideSearchbarTwoFingerprint.result

        //This should be the method aB in class fbn
        val targetMethodTwo =
            data.toMethodWalker(resultTwo!!.method).nextMethod(resultTwo!!.patternScanResult!!.startIndex, true).getMethod() as MutableMethod

        //Since both methods have the same smali code, inject instructions using a method.
        addInstructions(targetMethodTwo)

        return PatchResultSuccess()
    }

    private fun addInstructions(method: MutableMethod) {
        val index = method.implementation!!.instructions.size - 1
        method.addInstructions(
            index, """
            invoke-static {}, Lapp/revanced/integrations/patches/NewActionbarPatch;->getNewActionBar()Z
            move-result p0
        """
        )
    }

    //targetMethodOne: in class fbn
    /*
    .method public static aF(Ltxm;)Z
        invoke-virtual {p0}, Ltxm;->b()Lahah;
        move-result-object p0
        iget-object p0, p0, Lahah;->e:Lakfd;
        if-nez p0, :cond_a
        sget-object p0, Lakfd;->a:Lakfd;
        :cond_a
        iget-boolean p0, p0, Lakfd;->V:Z
        //added code here:
            invoke-static {}, app/revanced/integrations/patches/NewActionbarPatch;->getNewActionBar()Z
            move-result p0
        //original code here:
        return p0
    .end method
   */

    //targetMethodTwo: in class fbn
    /*
    .method public static aB(Ltxm;)Z
        invoke-virtual {p0}, Ltxm;->b()Lahah;
        move-result-object p0
        iget-object p0, p0, Lahah;->e:Lakfd;
        if-nez p0, :cond_a
        sget-object p0, Lakfd;->a:Lakfd;
        :cond_a
        iget-boolean p0, p0, Lakfd;->y:Z
        //added code here:
            invoke-static {}, app/revanced/integrations/patches/NewActionbarPatch;->getNewActionBar()Z
            move-result p0
        //original code here:
        return p0
    .end method

    */
}
