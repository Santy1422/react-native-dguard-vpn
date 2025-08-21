# Add project specific ProGuard rules here.

# ATOM SDK ProGuard rules
-dontwarn com.atom.sdk.**
-keep class com.atom.sdk.** { *; }
-keep interface com.atom.sdk.** { *; }

-dontwarn com.atom.core.**
-keep class com.atom.core.models.** { *; }
-keep interface com.atom.core.** { *; }

# Keep ATOM VPN module classes
-keep class com.atomvpn.** { *; }

# OpenVPN related
-keep class de.blinkt.openvpn.** { *; }
-keep class org.spongycastle.util.** { *; }
-keep class org.strongswan.android.** { *; }

# Gson specific rules
-keepattributes Signature
-keepattributes *Annotation*
-keep class sun.misc.Unsafe { *; }
-keep class com.google.gson.stream.** { *; }

# React Native rules
-keep class com.facebook.react.bridge.** { *; }

# General Android rules
-keepattributes *Annotation*
-keepattributes SourceFile,LineNumberTable
-keep public class * extends java.lang.Exception