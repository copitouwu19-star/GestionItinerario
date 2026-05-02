# Add project specific ProGuard rules here.
-keep class com.gestion.itinerario.** { *; }
-keepattributes *Annotation*
-keepclassmembers class * {
    @com.google.gson.annotations.SerializedName <fields>;
}
