# Keep kotlinx.serialization generated serializers.
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt

-keepclassmembers class kotlinx.serialization.json.** {
    *** Companion;
}
-keepclasseswithmembers class kotlinx.serialization.json.** {
    kotlinx.serialization.KSerializer serializer(...);
}

-keep,includedescriptorclasses class com.sakuva.app.**$$serializer { *; }
-keepclassmembers class com.sakuva.app.** {
    *** Companion;
}
-keepclasseswithmembers class com.sakuva.app.** {
    kotlinx.serialization.KSerializer serializer(...);
}
