# Consumer ProGuard rules for Engagement Cloud SDK
# These rules are bundled into the AAR and applied in consumer app builds.

-keep public class !com.sap.ec.EngagementCloud, com.sap.ec.** { *; }
