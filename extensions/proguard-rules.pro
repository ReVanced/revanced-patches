-dontobfuscate
-dontoptimize
-keepattributes *
-keep class app.revanced.** {
  *;
}
-keep class com.google.** {
  *;
}

-keep class org.mozilla.javascript.** { *; }
-keep class org.mozilla.classfile.ClassFileWriter
-dontwarn org.mozilla.javascript.tools.**