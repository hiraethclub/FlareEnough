# ProGuard and R8 rules.
#
# R8 shrinking is on for release and test builds. The default optimised rules,
# plus the keep rules the AndroidX libraries ship, cover almost everything. The
# rules below are the few this app needs on top.

# The Activity Result API drives the system file picker used by backup and export.
# Keep it and its contracts so shrinking never removes the launch path.
-keep class androidx.activity.result.** { *; }
-keep class androidx.activity.result.contract.** { *; }

# Backup, export, and report code is only reached when the person taps those
# actions, so keep it whole to be safe against over aggressive optimisation.
-keep class club.hiraeth.flareenough.data.backup.** { *; }
-keep class club.hiraeth.flareenough.export.** { *; }
