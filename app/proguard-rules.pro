# LinoVPN Proguard Rules

-keepattributes *Annotation*
-keepclassmembers class * {
    @androidx.room.* *;
}
-keep class ir.linovpn.app.data.local.entity.** { *; }
-keep class ir.linovpn.app.domain.model.** { *; }

# Native methods
-keepclasseswithmembernames class * {
    native <methods>;
}

# JNI Engines
-keep class ir.linovpn.xray.** { *; }
-keep class ir.linovpn.singbox.** { *; }
-keep class ir.linovpn.clash.** { *; }
-keep class ir.linovpn.v2ray.** { *; }
