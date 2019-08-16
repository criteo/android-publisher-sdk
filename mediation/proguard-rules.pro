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

-keep public class com.criteo.mediation.adapter.CriteoGoogleAdapter
-keepclassmembers class com.criteo.mediation.adapter.CriteoGoogleAdapter {
    public *;
}

-keep public class com.criteo.mediation.listener.CriteoBannerEventListenerImpl
-keepclassmembers class com.criteo.mediation.adapter.CriteoBannerEventListenerImpl {
    public *;
}

-keep public class com.criteo.mediation.listener.CriteoInterstitialEventListenerImpl
-keepclassmembers class com.criteo.mediation.adapter.CriteoInterstitialEventListenerImpl {
    public *;
}

-keep public class com.criteo.mediation.listener.MopubBannerListenerImpl
-keepclassmembers class com.criteo.mediation.adapter.MopubBannerListenerImpl {
    public *;
}

-keep public class com.criteo.mediation.listener.MopubInterstitialListenerImpl
-keepclassmembers class com.criteo.mediation.adapter.MopubInterstitialListenerImpl {
    public *;
}

-keep public enum com.criteo.mediation.model.FormatType { *; }

-keep public class com.criteo.mediation.mopubadapter.CriteoMopubBannerAdapter
-keepclassmembers class com.criteo.mediation.mopubadapter.CriteoMopubBannerAdapter {
    public *;
}

-keep public class com.criteo.mediation.mopubadapter.CriteoMopubBaseAdapterConfiguration
-keepclassmembers class com.criteo.mediation.mopubadapter.CriteoMopubBaseAdapterConfiguration {
    public *;
}

-keep public class com.criteo.mediation.mopubadapter.CriteoMopubInterstitialAdapter
-keepclassmembers class com.criteo.mediation.mopubadapter.CriteoMopubInterstitialAdapter {
    public *;
}
