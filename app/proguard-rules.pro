# Milestone 1 ships with minification disabled.
# Keep rules ready for Hilt and Room if release shrinking is turned on later.
-keep class dagger.hilt.** { *; }
-keep class androidx.room.** { *; }
