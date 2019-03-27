
# This file is intended to contain proguard options that *nobody* would ever
# not want, in *any* configuration - they ensure basic correctness, and have
# no downsides. You probably do not want to make changes to this file.

# The presence of both of these attributes causes dalvik and other jvms to print
# stack traces on uncaught exceptions, which is necessary to get useful crash
# reports.
-keepattributes SourceFile,LineNumberTable

# Preverification was introduced in Java 6 to enable faster classloading, but
# dex doesn't use the java .class format, so it has no benefit and can cause
# problems.
-dontpreverify

# Skipping analysis of some classes may make proguard strip something that's
# needed.
-dontskipnonpubliclibraryclasses

# Case-insensitive filesystems can't handle when a.class and A.class exist in
# the same directory.
-dontusemixedcaseclassnames

# This prevents the names of native methods from being obfuscated and prevents
# UnsatisfiedLinkErrors.
-keepclasseswithmembernames class * {
    native <methods>;
}

# hackbod discourages the use of enums on android, but if you use them, they
# should work. Allow instantiation via reflection by keeping the values method.
-keepclassmembers enum * {
    public static **[] values();
}

# Parcel reflectively accesses this field.
-keepclassmembers class * implements android.os.Parcelable {
  public static *** CREATOR;
}

# These methods are needed to ensure that serialization behaves as expected when
# classes are obfuscated, shrunk, and/or optimized.
-keepclassmembers class * implements java.io.Serializable {
    static final long serialVersionUID;
    private static final java.io.ObjectStreamField[] serialPersistentFields;
    private void writeObject(java.io.ObjectOutputStream);
    private void readObject(java.io.ObjectInputStream);
    java.lang.Object writeReplace();
    java.lang.Object readResolve();
}

# Don't warn about Guava. Any Guava-using app will fail the proguard stage without this dontwarn,
# and since Guava is so widely used, we include it here in the base.
-dontwarn com.google.common.**

# Don't warn about Error Prone annotations (e.g. @CompileTimeConstant)
-dontwarn com.google.errorprone.annotations.**

# Based on http://ag/718466: android.app.Notification.setLatestEventInfo() was
# removed in MNC, but is still referenced (safely) by the NotificationCompat
# code.
-dontwarn android.app.Notification

# Silence notes about dynamically referenced classes from AOSP support
# libraries.
-dontnote android.graphics.Insets

# AOSP support library:  ICU references to gender and plurals messages.
-dontnote libcore.icu.ICU
-keep class libcore.icu.ICU { *** get(...);}

# AOSP support library:  Handle classes that use reflection.
-dontnote android.support.v4.app.NotificationCompatJellybean

-assumenosideeffects class app.diol.dialer.common.LogUtil {
    public static void v(...);
    public static void d(...);
}

# Used for building release binaries. Obfuscates, optimizes, and shrinks.

# By default, proguard leaves all classes in their original package, which
# needlessly repeats com.google.android.apps.etc.
-repackageclasses ''

# Allows proguard to make private and protected methods and fields public as
# part of optimization. This lets proguard inline trivial getter/setter methods.
-allowaccessmodification

# The source file attribute must be present in order to print stack traces, but
# we rename it in order to avoid leaking the pre-obfuscation class name.
-renamesourcefileattribute PG

# This allows proguard to strip isLoggable() blocks containing only debug log
# code from release builds.
-assumenosideeffects class android.util.Log {
  static *** i(...);
  static *** d(...);
  static *** v(...);
  static *** isLoggable(...);
}

# This allows proguard to strip Trace code from release builds.
-assumenosideeffects class android.os.Trace {
  static *** beginSection(...);
  static *** endSection(...);
}

# Keep the annotation, classes, methods, and fields marked as UsedByReflection
-keep class app.diol.dialer.proguard.UsedByReflection
-keep @app.diol.dialer.proguard.UsedByReflection class *
-keepclassmembers class * {
    @app.diol.dialer.proguard.UsedByReflection *;
}
-keep class android.support.design.widget.FloatingActionButton$* { *; }