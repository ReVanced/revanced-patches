package app.revanced.patches.youtube.layout.autorepeat.fingerprints

import app.revanced.patcher.annotation.Name
import app.revanced.patcher.annotation.Version
import app.revanced.patcher.extensions.or
import app.revanced.patcher.fingerprint.method.impl.MethodFingerprint
import app.revanced.patcher.fingerprint.method.annotation.FuzzyPatternScanMethod
import app.revanced.patcher.fingerprint.method.annotation.MatchingMethod
import app.revanced.patches.youtube.layout.autorepeat.annotations.AutoRepeatCompatibility
import org.jf.dexlib2.AccessFlags

@Name("auto-repeat-parent-fingerprint")
@MatchingMethod(
    "Laamp;", "E"
)
@FuzzyPatternScanMethod(2)
@AutoRepeatCompatibility
@Version("0.0.1")
//This Fingerprints finds the play() method needed to be called when AutoRepeatPatch.shouldAutoRepeat() == true
/*
public final void E() {
Stuff happens
String str = "play() called when the player wasn't loaded.";
String str2 = "play() blocked because Background Playability failed";
Stuff happens again
}
 */
object AutoRepeatParentFingerprint : MethodFingerprint(
    "V",
    AccessFlags.PUBLIC or AccessFlags.FINAL,
    null,
    null,
    listOf("play() called when the player wasn't loaded.", "play() blocked because Background Playability failed"),
    null
)