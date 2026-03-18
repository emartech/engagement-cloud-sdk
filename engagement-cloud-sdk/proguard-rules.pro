# Consumer ProGuard rules for Engagement Cloud SDK
# These rules are bundled into the AAR and applied in consumer app builds.

# Remove the EngagementCloud class from the Android build
-keep public class !com.sap.ec.EngagementCloud, com.sap.ec.** { *; }
