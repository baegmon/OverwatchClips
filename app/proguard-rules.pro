-dontwarn com.squareup.okhttp.**
-dontwarn org.sonatype.**
-dontwarn org.**
-dontwarn java.**
-dontwarn javax.**
-dontwarn com.jcraft.**

-keepattributes Signature
-keepattributes *Annotation*
-keep class sun.misc.Unsafe { *; }

-dontwarn sun.misc.Unsafe
-dontwarn com.google.common.**