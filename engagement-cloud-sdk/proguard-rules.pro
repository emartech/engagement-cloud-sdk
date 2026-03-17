# Consumer ProGuard rules for Engagement Cloud SDK
# These rules are bundled into the AAR and applied in consumer app builds.

# Keep all public classes in com.sap.ec.** for SDK consumers.
# The common EngagementCloud object (com.sap.ec.EngagementCloud) is excluded because
# Android consumers should use com.sap.ec.android.EngagementCloud instead.
# Note: This rule is intentionally broad (keeps all members). A future optimization
# could tighten to `-keep public class com.sap.ec.** { public *; }` to allow R8
# to strip internal members in consumer builds.
-keep public class !com.sap.ec.EngagementCloud, com.sap.ec.** { *; }
