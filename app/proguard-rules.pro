# OkHttp 3 / 4
-dontwarn okhttp3.**
-dontwarn okio.**
-dontwarn javax.annotation.**
-keepnames class okhttp3.internal.publicsuffix.PublicSuffixDatabase

# Kotlin Coroutines
-dontwarn kotlinx.coroutines.**

# Keep data models
-keep class com.ce46.connectme.data.** { *; }

# Keep Android Compose annotations and line numbers for stack traces
-keepattributes *Annotation*,Signature,InnerClasses,EnclosingMethod
-keepattributes SourceFile,LineNumberTable
