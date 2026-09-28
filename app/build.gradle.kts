import java.io.FileInputStream
import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.ksp)
    alias(libs.plugins.room)
}

// Release signing material. The key is never committed. It is provided either by a
// local keystore.properties file (for building on your own machine) or by environment
// variables in CI, which come from GitHub secrets. Both are listed in .gitignore.
val keystorePropertiesFile = rootProject.file("keystore.properties")
val keystoreProperties = Properties().apply {
    if (keystorePropertiesFile.exists()) {
        FileInputStream(keystorePropertiesFile).use { load(it) }
    }
}
val releaseStorePath: String? = System.getenv("KEYSTORE_FILE")
    ?: keystoreProperties.getProperty("storeFile")
val hasReleaseKey = releaseStorePath != null

android {
    namespace = "club.hiraeth.flareenough"
    compileSdk = libs.versions.compileSdk.get().toInt()

    defaultConfig {
        applicationId = "club.hiraeth.flareenough"
        minSdk = libs.versions.minSdk.get().toInt()
        targetSdk = libs.versions.targetSdk.get().toInt()
        versionCode = 3
        versionName = "0.1.2"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        // The launcher label. Normal builds show the real app name from the single
        // string resource. The test build type overrides this placeholder so the
        // two apps can be told apart on the home screen. The production name still
        // lives only in strings.xml (R.string.app_name); nothing here changes it.
        manifestPlaceholders["appLabel"] = "@string/app_name"
    }

    // Android's build tool adds a "dependency metadata" block to the APK by default,
    // aimed at Google Play. It is not wanted here and F-Droid rejects it, so turn it
    // off. This keeps the built APK clean and has no effect on how the app works.
    dependenciesInfo {
        includeInApk = false
        includeInBundle = false
    }

    signingConfigs {
        // Only created when a key is actually available, so ordinary debug builds
        // and CI need no signing material.
        if (hasReleaseKey) {
            create("release") {
                storeFile = file(releaseStorePath!!)
                storePassword = System.getenv("KEYSTORE_PASSWORD")
                    ?: keystoreProperties.getProperty("storePassword")
                keyAlias = System.getenv("KEY_ALIAS")
                    ?: keystoreProperties.getProperty("keyAlias")
                keyPassword = System.getenv("KEY_PASSWORD")
                    ?: keystoreProperties.getProperty("keyPassword")
            }
        }
    }

    buildTypes {
        release {
            // R8 shrinks and optimises the release build, removing unused code and
            // making the download smaller. The default optimised rules keep enums
            // (values and valueOf, which the database converters rely on), and the
            // AndroidX libraries ship their own keep rules, so no app specific rules
            // are needed. Resource shrinking stays off for now to avoid removing any
            // resource that is only referenced by name.
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
            // Sign with the real release key when it is present, for example in the
            // release workflow. Falls back to the debug key so a local assembleRelease
            // still produces an installable APK for testing.
            signingConfig = if (hasReleaseKey) {
                signingConfigs.getByName("release")
            } else {
                signingConfigs.getByName("debug")
            }
        }

        // A side by side test build. It behaves exactly like a real release (R8
        // shrinking and the same rules, inherited from the release type above), but
        // installs as a separate app so you can keep the real one for daily use.
        //
        //   - A different application id (club.hiraeth.flareenough.test) makes Android
        //     treat it as a distinct app, with its own data, alarms, and icon. It
        //     never touches the real app's medications or history.
        //   - A "(Test)" launcher label and a "-test" version so it is easy to tell
        //     apart from the live app.
        //   - Always signed with the debug key, so this build needs no signing secret
        //     and never uses the real release key. The differing application id means
        //     there is no signature clash with the installed release app.
        create("testRelease") {
            initWith(getByName("release"))
            applicationIdSuffix = ".test"
            versionNameSuffix = "-test"
            signingConfig = signingConfigs.getByName("debug")
            manifestPlaceholders["appLabel"] = "Flare Enough (Test)"
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    buildFeatures {
        compose = true
    }

    // Make the exported Room schemas available to instrumented migration tests as
    // assets, so MigrationTestHelper can find them on the device.
    sourceSets {
        getByName("androidTest") {
            assets.srcDir("$projectDir/schemas")
        }
    }

    // Dependency licence report tooling and Compose test manifests can duplicate
    // these files. Excluding them avoids a packaging clash.
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

// Kotlin compiler options. Declared at the top level (not inside android {}) as
// recommended for Kotlin 2.x. Java 11 bytecode matches the compileOptions above.
kotlin {
    compilerOptions {
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_11)
    }
}

// Room exports the database schema as JSON to this folder on every build. The
// files are committed so migrations can be written and tested against them.
room {
    schemaDirectory("$projectDir/schemas")
}

dependencies {
    // Pure Kotlin scheduling logic.
    implementation(project(":schedule"))

    // Core Android and lifecycle.
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.activity.compose)

    // Room database. The KSP compiler generates the DAO and database code.
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)

    // DataStore for simple settings.
    implementation(libs.androidx.datastore.preferences)

    // Compose. The BOM keeps every Compose library on one tested version set.
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material.icons.extended)
    implementation(libs.androidx.navigation.compose)

    // Unit tests.
    testImplementation(libs.junit4)

    // Instrumented and Compose UI tests.
    androidTestImplementation(libs.androidx.test.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)

    // Room instrumented tests (DAO behaviour and migrations on a device).
    androidTestImplementation(libs.androidx.room.testing)

    // Compose tooling, debug builds only.
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
}
