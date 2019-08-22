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
-keepparameternames

-keep public class com.criteo.mediation.google.CriteoAdapter
-keepclassmembers class com.criteo.mediation.google.CriteoAdapter {
    public *;
}

-keep public class com.criteo.mediation.google.CriteoBannerEventListener
-keepclassmembers class com.criteo.mediation.google.CriteoBannerEventListener {
    public *;
}

-keep public class com.criteo.mediation.google.CriteoInterstitialEventListener
-keepclassmembers class com.criteo.mediation.google.CriteoInterstitialEventListener {
    public *;
}

-keep public class com.criteo.mediation.mopub.CriteoBannerEventListener
-keepclassmembers class com.criteo.mediation.mopub.CriteoBannerEventListener {
    public *;
}

-keep public class com.criteo.mediation.mopub.CriteoInterstitialEventListener
-keepclassmembers class com.criteo.mediation.mopub.CriteoInterstitialEventListener {
    public *;
}

-keep public class com.criteo.mediation.mopub.CriteoBannerAdapter
-keepclassmembers class com.criteo.mediation.mopub.CriteoBannerAdapter {
    public *;
}

-keep public class com.criteo.mediation.mopub.CriteoBaseAdapterConfiguration
-keepclassmembers class com.criteo.mediation.mopub.CriteoBaseAdapterConfiguration {
    public *;
}

-keep public class com.criteo.mediation.mopub.CriteoInterstitialAdapter
-keepclassmembers class com.criteo.mediation.mopub.CriteoInterstitialAdapter {
    public *;
}
