# Remove the Emarsys class from the Android build
-keep public class !com.emarsys.Emarsys, com.emarsys.** { *; }

-dontwarn java.lang.invoke.StringConcatFactory