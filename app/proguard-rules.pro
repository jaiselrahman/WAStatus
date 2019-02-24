# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Uncomment this to preserve the line number information for
# debugging stack traces.
-keepattributes SourceFile,LineNumberTable
-keep public class * extends java.lang.Exception

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile


-dontwarn okhttp3.internal.platform.*
-dontwarn com.squareup.okhttp3.**
-keep class com.squareup.okhttp.* { *; }
-dontwarn okio.**

-keepattributes Signature,RuntimeVisibleAnnotations,AnnotationDefault
-keepclassmembers class * {
    @com.google.api.client.util.Key <fields>;
}
-dontwarn com.google.api.client.googleapis.extensions.android.**
-dontwarn com.google.android.gms.**
-dontnote java.nio.file.Files, java.nio.file.Path
-dontnote **.ILicensingService
-dontnote sun.misc.Unsafe
-dontwarn sun.misc.Unsafe

-dontwarn com.google.errorprone.annotations.**
-dontwarn com.google.common.collect.**
-dontwarn com.google.common.base.Splitter


-keep public class com.google.android.material.bottomnavigation.** { *; }
-dontnote com.google.android.material.**
-dontwarn com.google.android.material.**

#-keep public class * implements org.mp4parser.Box { *; }
-keep public class org.mp4parser.boxes.iso14496.part12.* { *; }
-keep public class org.mp4parser.boxes.iso14496.part14.ESDescriptorBox { *; }
-keep public class org.mp4parser.boxes.iso14496.part15.AvcConfigurationBox { *; }
-keep public class org.mp4parser.boxes.sampleentry.VisualSampleEntry { *; }
-keep public class org.mp4parser.boxes.sampleentry.AudioSampleEntry { *; }
-keep public class org.mp4parser.boxes.apple.PixelAspectRationAtom { *; }
-keep public class org.mp4parser.boxes.UnknownBox { *; } 
-dontwarn org.mp4parser.**

-dontwarn org.junit.internal.runners.**
-dontwarn org.junit.rules.**
-dontwarn org.slf4j.**
-dontwarn android.test.**
