# DARKBOX / BLACKBOX CORE (NO OBFUSCATION)
-keep class com.darkbox.** { *; }
-keep class com.darkbox.VBoxCore { *; }
-keep class com.darkbox.fake.** { *; }
-keep class com.darkbox.fake.delegate.** { *; }
-keep class com.darkbox.fake.hook.** { *; }
-keep class com.darkbox.fake.service.** { *; }
-keep class com.darkbox.fake.service.context.providers.** { *; }
-keep class com.darkbox.fake.provider.** { *; }
-keep class com.darkbox.fake.frameworks.** { *; }
-keep class com.darkbox.entity.** { *; }
-keep class com.darkbox.app.** { *; }
-keep class com.darkbox.core.** { *; }
-keep class com.darkbox.utils.** { *; }
-keep class com.darkbox.proxy.** { *; }
-keep class com.darkbox.jnihook.** { *; }

-keepclassmembers class * {
    native <methods>;
}
