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
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile
# Retrofit does reflection on generic parameters. InnerClasses is required to use Signature and
# EnclosingMethod is required to use InnerClasses.
-keepattributes Signature, InnerClasses, EnclosingMethod, Exceptions

# Retain service method parameters when optimizing.
-keepclassmembers,allowshrinking,allowobfuscation interface * {
    @retrofit2.http.* <methods>;
}

# Ignore annotation used for build tooling.
-dontwarn org.codehaus.mojo.animal_sniffer.IgnoreJRERequirement

# Ignore JSR 305 annotations for embedding nullability information.
-dontwarn javax.annotation.**

# Guarded by a NoClassDefFoundError try/catch and only used when on the classpath.
-dontwarn kotlin.Unit

# Top-level functions that can only be used by Kotlin.
-dontwarn retrofit2.-KotlinExtensions

# Leaving Criteo SDK models.
-keepparameternames
-keep public class com.criteo.publisher.Criteo
-keepclassmembers class com.criteo.publisher.Criteo {
   public *;
}

-keep class com.criteo.publisher.Criteo$Builder {
  public *;
}

-keep public class com.criteo.publisher.CriteoBannerView
-keepclassmembers class com.criteo.publisher.CriteoBannerView {
   public *;
}

-keep public class com.criteo.publisher.CriteoInterstitial
-keepclassmembers class com.criteo.publisher.CriteoInterstitial {
   public *;
}

-keep public class com.criteo.publisher.CriteoErrorCode
-keepclassmembers class com.criteo.publisher.CriteoErrorCode {
   public *;
}

-keep public class com.criteo.publisher.BidResponse
-keepclassmembers class com.criteo.publisher.BidResponse {
   public *;
}

-keep public class com.criteo.publisher.BidToken

-keep public class com.criteo.publisher.CriteoInitException

-keep public interface com.criteo.publisher.CriteoAdListener {*;}

-keep public interface com.criteo.publisher.CriteoBannerAdListener {*;}

-keep class com.google.android.gms.ads.** { *; }




