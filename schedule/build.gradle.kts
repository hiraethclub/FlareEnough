// Pure Kotlin module. No Android plugin, no Android dependencies. This lets the
// daylight saving safe scheduling logic be unit tested on a plain JVM, which is
// faster and more reliable than an Android test, and keeps the critical logic
// isolated from the UI.
plugins {
    alias(libs.plugins.kotlin.jvm)
}

dependencies {
    testImplementation(libs.junit.jupiter)
    testRuntimeOnly(libs.junit.platform.launcher)
}

kotlin {
    jvmToolchain(21)
}

tasks.test {
    useJUnitPlatform()
}
