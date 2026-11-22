# Scroll the Gita - Production R8 / Proguard Rules
# Do not use blanket -dontobfuscate. Obfuscation remains active for security & binary size.

# ==========================================
# 1. Android & Kotlin Core
# ==========================================
-keepattributes *Annotation*,Signature,InnerClasses,EnclosingMethod,SourceFile,LineNumberTable

# Preserve Enum values and valueOf methods (used in themes, statuses, etc.)
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

# Preserve Parcelable CREATORs
-keepclassmembers class * implements android.os.Parcelable {
    static ** CREATOR;
}

# ==========================================
# 2. Room Database, Entities, DAOs & Converters
# ==========================================
# Preserve Room database definitions and generated delegates
-keep class * extends androidx.room.RoomDatabase {
    <init>(...);
}

# Preserve all Room entities and their constructors/fields
-keep @androidx.room.Entity class * { *; }
-keep class com.scrollthegita.core.database.*Entity { *; }

# Preserve all Room DAOs and their generated implementations
-keep @androidx.room.Dao interface * { *; }
-keep interface com.scrollthegita.core.database.*Dao { *; }
-keep class * implements androidx.room.RoomOpenDelegate

# Preserve TypeConverters methods
-keepclassmembers class * {
    @androidx.room.TypeConverter *;
}

# Suppress benign Room/Paging warnings
-dontwarn androidx.room.paging.**

# ==========================================
# 3. Jetpack Compose
# ==========================================
# Keep Composable annotations and functions
-keepclassmembers class * {
    @androidx.compose.runtime.Composable *;
}

# Keep Compose Preview providers and UI tooling interfaces
-keep class * implements androidx.compose.ui.tooling.preview.PreviewParameterProvider

# ==========================================
# 4. Hilt & Dependency Injection
# ==========================================
-keep class dagger.hilt.** { *; }
-keep class * extends dagger.hilt.internal.GeneratedComponentManager
-keep class * extends dagger.hilt.internal.ComponentManager
-keepclassmembers class * {
    @javax.inject.Inject *;
    @dagger.Provides *;
    @dagger.Binds *;
}

# ==========================================
# 5. DataStore & Coroutines
# ==========================================
-dontwarn kotlinx.coroutines.**
-keepclassmembers class * extends androidx.datastore.preferences.core.Preferences {
    *;
}
