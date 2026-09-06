# Keep JNI Methods & Models
-keepclasseswithmembernames class * {
    native <methods>;
}

-keep class com.auramotion.editor.engine.** { *; }
-keep class com.auramotion.editor.model.** { *; }
