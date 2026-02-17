# Remove the EngagementCloud class from the Android build
-keep public class !com.sap.ec.EngagementCloud, com.sap.ec.** { *; }

-dontwarn java.lang.invoke.StringConcatFactory