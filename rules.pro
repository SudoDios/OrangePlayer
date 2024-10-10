-keep class com.sun.jna.** { *; }
-keep class uk.co.caprica.vlcj.** { *; }
-keep class org.jaudiotagger.tag.** { *; }
-keep class kotlinx.coroutines.swing.SwingDispatcherFactory
-keep class kotlinx.datetime.** { *; }
-keep class kotlinx.coroutines.** { *; }
-keep class kotlinx.serialization.** { *; }
-keep class androidx.compose.runtime.** { *; }
-keep class com.github.kwhat.jnativehook.** { *; }

-keep class me.sudodios.mediainfo.** { *; }
-keep class me.sudodios.orangeplayer.models.** { *; }
-keep class me.sudodios.orangeplayer.core.Native {
    native <methods>;
}

-dontwarn kotlinx.datetime.**
-dontwarn androidx.compose.**
-dontwarn kotlinx.atomicfu.**
-dontwarn org.apache.log4j.**