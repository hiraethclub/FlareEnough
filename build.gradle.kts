// Top level build file. Plugins are declared here without applying them, so each
// module can apply just the ones it needs. This keeps module build files short.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.jvm) apply false
    alias(libs.plugins.kotlin.compose) apply false
    // KSP and Room plugins are added in Milestone 2 (the database), with a KSP
    // version verified against the chosen Kotlin version at that time.
}
