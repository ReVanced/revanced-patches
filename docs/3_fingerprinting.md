# 🔎 Fingerprinting

Fingerprinting is the process of creating uniquely identifiable data about something arbitrarily large. In the context of ReVanced, fingerprinting is essential to be able to find classes, methods and fields without knowing their original names or certain other attributes, which would be used to identify them under normal circumstances.

## ⛳️ Example fingerprint

This page works with the following fingerprint as an example:

```kt

package app.revanced.patches.ads.fingerprints

// Imports

object LoadAdsFingerprint : MethodFingerprint(
    returnType = "Z",
    access = AccessFlags.PUBLIC or AccessFlags.FINAL,
    parameters = listOf("Z"),
    opcodes = listOf(Opcode.RETURN),
    strings = listOf("pro"),
    customFingerprint = { it.definingClass == "Lcom/some/app/ads/Loader;"}
)
```

## 🆗 Understanding the example fingerprint

The example fingerprint called `LoadAdsFingerprint`, which extends on [`MethodFingerprint`](https://github.com/revanced/revanced-patcher/blob/d2f91a8545567429d64a1bcad6ca1dab62ec95bf/src/main/kotlin/app/revanced/patcher/fingerprint/method/impl/MethodFingerprint.kt#L28) is made to uniquely identify a certain method by capturing various attributes of the method such as the return type, access flags, an opcode pattern and more. The following Java code can be reconstructed from the fingerprint:

```java
    import com.some.app.ads;

    // Imports

 4 <attributes> class Loader {
 5     public final boolean <methodName>(boolean <field>) {
           // ...

 8         var userStatus = "pro";

           // ...

12         return <returnValue>;
       }
    }
```

## 🚀 How it works

Each fingerprint attribute describes a specific but distinct part of the method.
The combination out of those should be and ideally remain unique to all methods in all classes.
In the case of the example fingerprint, the `customFingerprint` attribute is responsible for finding the class
the method is defined in. This greatly increases the uniqueness of the fingerprint because the possible methods
are now reduced to that class. Adding the signature of the method and a string the method implementation refers to in
combination now creates a unique fingerprint in the current example:

- Package & class (Line 4)

  ```kt
  customFingerprint = { it.definingClass == "Lcom/some/app/ads/Loader;"}
  ```

- Method signature (Line 5)

  ```kt
  returnType = "Z",
  access = AccessFlags.PUBLIC or AccessFlags.FINAL,
  parameters = listOf("Z"),
  ```

- Method implementation (Line 8 & 12)

  ```kt
  strings = listOf("pro"),
  opcodes = listOf(Opcode.RETURN)
  ```

## 🔨 How to use fingerprints

After creating a fingerprint, add it to the constructor of the `BytecodePatch`:

```kt
object DisableAdsPatch : BytecodePatch(
    setOf(LoadAdsFingerprint)
) { /* .. */ }
```

ReVanced Patcher will try to [resolve the fingerprint](https://github.com/ReVanced/revanced-patcher/blob/67b7dff67a212b4fc30eb4f0cbe58f0ba09fb09a/revanced-patcher/src/main/kotlin/app/revanced/patcher/fingerprint/method/impl/MethodFingerprint.kt#L182)
**before** it calls the `execute` method of the patch.

The fingerprint can now be used in the patch by accessing [`MethodFingerprint.result`](https://github.com/ReVanced/revanced-patcher/blob/67b7dff67a212b4fc30eb4f0cbe58f0ba09fb09a/revanced-patcher/src/main/kotlin/app/revanced/patcher/fingerprint/method/impl/MethodFingerprint.kt#L44):

```kt
object DisableAdsPatch : BytecodePatch(
  setOf(LoadAdsFingerprint)
) {
    override fun execute(context: BytecodeContext) {
        val result = LoadAdsFingerprint.result
            ?: throw PatchException("LoadAdsFingerprint not found")

        // ...
    }
}
```

> **Note**: `MethodFingerprint.result` **can be null** if the fingerprint does not match any method.
> In such cases, the fingerprint needs to be fixed and made more resilient if a later version causes the error
> of an app in which the fingerprint was not tested. A fingerprint is good if it is _light_,
> but still resilient - like Carbon fibre-reinforced polymers.

If the fingerprint resolved to a method, the following properties are now available:

```kt
data class MethodFingerprintResult(
    val method: Method,
    val classDef: ClassDef,
    val scanResult: MethodFingerprintScanResult,
    // ...
) {
    val mutableClass
    val mutableMethod

    // ...
}
```

> Details on how to use them in a patch and what exactly these are will be introduced properly later on this page.

## 🏹 Different ways to resolve a fingerprint

Usually, fingerprints are resolved by ReVanced Patcher, but it is also possible to manually resolve a
fingerprint in a patch. This can be quite useful in lots of situations. To resolve a fingerprint, you need
a `BytecodeContext` to resolve it. This context contains classes and, thus, methods against which the fingerprint
can be resolved. Example: _You have a fingerprint which you manually want to resolve
**without** the help of ReVanced Patcher._

> **Note**: A fingerprint should not be added to the constructor of `BytecodePatch` if manual resolution is intended,
> because ReVanced Patcher would try to resolve it before manual resolution.

- On a **list of classes** using [`MethodFingerprint.resolve`](https://github.com/ReVanced/revanced-patcher/blob/67b7dff67a212b4fc30eb4f0cbe58f0ba09fb09a/revanced-patcher/src/main/kotlin/app/revanced/patcher/fingerprint/method/impl/MethodFingerprint.kt#L263)

  This can be useful if a fingerprint should be resolved to a smaller subset of classes, 
  Otherwise, the fingerprint can be resolved automatically by ReVanced Patcher.

  ```kt
  object DisableAdsPatch : BytecodePatch(
      /* setOf(LoadAdsFingerprint) */
  ) {
      override fun execute(context: BytecodeContext) {
          val result = LoadAdsFingerprint.also { it.resolve(context, context.classes) }.result
              ?: throw PatchException("LoadAdsFingerprint not found")

          // ...
      }
  }
  ```

- On a **single class** using [`MethodFingerprint.resolve`](https://github.com/revanced/revanced-patcher/blob/d2f91a8545567429d64a1bcad6ca1dab62ec95bf/src/main/kotlin/app/revanced/patcher/fingerprint/method/impl/MethodFingerprint.kt#L63)

  Sometimes, you know a class, but you need certain methods. In such cases, you can resolve fingerprints on a class.

  ```kt
   object DisableAdsPatch : BytecodePatch(
      setOf(LoadAdsFingerprint)
  ) {
      override fun execute(context: BytecodeContext) {
          val adsLoaderClass = context.classes.single { it.name == "Lcom/some/app/ads/Loader;" }

          val result = LoadAdsFingerprint.also { it.resolve(context, adsLoaderClass) }.result
              ?: throw PatchException("LoadAdsFingerprint not found")

          // ...
      }
  }
  ```

- On a **method** using [`MethodFingerprint.resolve`](https://github.com/revanced/revanced-patcher/blob/d2f91a8545567429d64a1bcad6ca1dab62ec95bf/src/main/kotlin/app/revanced/patcher/fingerprint/method/impl/MethodFingerprint.kt#L78)

  Resolving a fingerprint on a method is mostly only useful
  if the fingerprint is used to resolve certain information about a method, such as `MethodFingerprintResult.scanResult`.
  Example: _A fingerprint should be used to resolve the method which loads ads.
  For that, the fingerprint is added to the constructor of `BytecodePatch`.
  An additional fingerprint is responsible for finding the indices of the instructions with certain string references
  in implementing the method the first fingerprint resolved to._

  ```kt
  class DisableAdsPatch : BytecodePatch(
      /* listOf(LoadAdsFingerprint) */
  ) {
      override fun execute(context: BytecodeContext) {
          // Make sure this fingerprint succeeds as the result is required
          val adsFingerprintResult = LoadAdsFingerprint.result
              ?: throw PatchException("LoadAdsFingerprint not found")

          // Additional fingerprint to get the indices of two strings
          val proStringsFingerprint = object : MethodFingerprint(
              strings = listOf("free", "trial")
          ) {}

          proStringsFingerprint.also {
              // Resolve the fingerprint on the first fingerprints method
              it.resolve(context, adsFingerprintResult.method)
          }.result?.let { result ->
              // Use the fingerprints result
              result.scanResult.stringsScanResult!!.matches.forEach { match ->
                      println("The index of the string '${match.string}' is ${match.index}")
                  }

          } ?: throw PatchException("pro strings fingerprint not found")
      }
  }
  ```

## 🎯 The result of a fingerprint

After a `MethodFingerprint` resolves successfully, its result can be used.
The result contains mutable and immutable references to the method and its defined class.

> **Warning**: By default, the immutable references **should be used** to prevent a mutable copy of the immutable references. For a patch to properly use a fingerprint, though, usually write access is required. For that, mutable references can be used.

Among them, the result also contains [MethodFingerprintResult.scanResult](https://github.com/revanced/revanced-patcher/blob/d2f91a8545567429d64a1bcad6ca1dab62ec95bf/src/main/kotlin/app/revanced/patcher/fingerprint/method/impl/MethodFingerprint.kt#L239) which contains additional useful properties:

```kt
data class MethodFingerprintScanResult(
    val patternScanResult: PatternScanResult?,
    val stringsScanResult: StringsScanResult?
) {
    data class PatternScanResult(
        val startIndex: Int,
        val endIndex: Int,
        var warnings: List<Warning>? = null
    )

    data class StringsScanResult(val matches: List<StringMatch>){
        data class StringMatch(val string: String, val index: Int)
    }

    // ...
}
```

Bytecode patches utilize the following properties:

- The `MethodFingerprint.strings` allows patches to know the indices of the instructions
  which hold references to the strings.

- If a fingerprint defines `MethodFingerprint.opcodes`, the start and end index of the first instructions
  matching that pattern will be available. These are useful to patch the implementation of methods
  relative to the pattern. Ideally, the pattern contains instructions for patching the opcodes pattern to guarantee a successful patch.

  > **Note**: Sometimes long patterns might be necessary, but the bigger the pattern list, the higher the chance
  it mutates if the app updates. Therefore, the annotation `FuzzyPatternScanMethod` can be used
  on a fingerprint. The `FuzzyPatternScanMethod.threshold` will define how many opcodes can remain unmatched.
  If necessary, `PatternScanResult.warnings` can then be used to know where pattern mismatches occurred.

## ⭐ Closely related code examples

### 🧩 Patches

- [`VideoAdsPatch`](https://github.com/ReVanced/revanced-patches/blob/7c431c867d62f024855bb07f0723dbbf0af034ae/src/main/kotlin/app/revanced/patches/twitch/ad/video/VideoAdsPatch.kt)
- [`RememberVideoQualityPatch`](https://github.com/ReVanced/revanced-patches/blob/7c431c867d62f024855bb07f0723dbbf0af034ae/src/main/kotlin/app/revanced/patches/youtube/video/quality/RememberVideoQualityPatch.kt)

### 🔍 Fingerprints

- [`LoadVideoAdsFingerprint`](https://github.com/revanced/revanced-patches/blob/2d10caffad3619791a0c3a670002a47051d4731e/src/main/kotlin/app/revanced/patches/youtube/ad/video/fingerprints/LoadVideoAdsFingerprint.kt)
- [`SeekbarTappingParentFingerprint`](https://github.com/revanced/revanced-patches/blob/2d10caffad3619791a0c3a670002a47051d4731e/src/main/kotlin/app/revanced/patches/youtube/interaction/seekbar/fingerprints/SeekbarTappingParentFingerprint.kt)

## ⏭️ Whats next

The next section will give a suggestion on coding conventions and the file structure of a patch.

Continue: [📜 Patch file structure and conventions](4_structure_and_conventions.md)
