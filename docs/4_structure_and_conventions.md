# 📜 Patch file structure and conventions

ReVanced follows a couple of conventions when creating patches which can be found in [ReVanced Patches](https://github.com/revanced/revanced-patches).

## 📁 File structure

Each patch is structured the following way:

```text
📦your.patches.app.category.patch
 ├ 📂fingerprints
 ├ ├ 🔍SomeFingerprintA.kt
 ├ └ 🔍SomeFingerprintB.kt
 └ 🧩SomePatch.kt
```

### 🆗 Example

As an example the structure of [`RememberVideoQualityPatch`](https://github.com/ReVanced/revanced-patches/blob/7c431c867d62f024855bb07f0723dbbf0af034ae/src/main/kotlin/app/revanced/patches/youtube/video/quality/RememberVideoQualityPatch.kt) can be used as a reference:

```text
📦app.revanced.patches.youtube.ad.video
 ├ 📂fingerprints
 ├ ├ 🔍NewVideoQualityChangedFingerprint.kt
 ├ ├ 🔍SetQualityByIndexMethodClassFieldReferenceFingerprint.kt
 ├ ├ 🔍VideoQualityItemOnClickParentFingerprint.kt
 ├ └ 🔍VideoQualitySetterFingerprint.kt
 └ 🧩RememberVideoQualityPatch.kt
```

## 📙 Conventions

> **Note**: More ⭐ equals more importance

- ⭐⭐ **Patches should be named by what they accomplish**.
  Example: _To patch ads on videos, the patch should be called `HideVideoAdsPatch`._

- ⭐⭐ **`@Description` should be written in third person and end with punctuation**.
  Example: _Removes ads in the video player._

- ⭐ **Resource and bytecode patches should be properly separated**.
  That means, bytecode patches handle patching bytecode, while resource patches handle resources.
  As an example, [`SponsorBlockPatch`](https://github.com/revanced/revanced-patches/tree/2d10caffad3619791a0c3a670002a47051d4731e/src/main/kotlin/app/revanced/patches/youtube/layout/sponsorblock) can be used.

- ⭐⭐⭐ **Allocate as little code as possible in patches**.
  This reduces the risk of failing patches. In the example of [`SponsorBlockPatch`](https://github.com/revanced/revanced-patches/tree/2d10caffad3619791a0c3a670002a47051d4731e/src/main/kotlin/app/revanced/patches/youtube/layout/sponsorblock),
  most of the code logic is written in [revanced-integrations](https://github.com/revanced/revanced-integrations).
  The patches now only insert references to public methods from the integrations
  which are merged into the app which is far better than writing huge bytecode patches.

- ⭐⭐⭐ **Create small but strong fingerprints**.
  This is essential for patches to last long, because fingerprints
  create the foundation for patches to find the places where patches need to be done.
  A small fingerprint guarantees that it remains intact in case the app updates and code mutates,
  but can also can cause problems if it is not unique enough and for example resolve
  to a wrong method or give the wrong indices of instructions if a pattern is used.
  A fingerprint consisting out of couple distinct strings is a small but strong fingerprint,
  on the other hand, a fingerprint which contains a huge list of opcodes can be strong,
  but is likely fail to resolve in the future because the instructions could mutate with
  an update of the app.

- ⭐⭐⭐ **Document patches**. This is essential as a future reference when reading the code.
  Explaining what certain patches do and accomplish guarantees,
  that the code can be understood in the future in the case it needs to be updated.
  Example code comment: _Patch the return value to true in order to spoof the pro status of the user.This turns off ads._
