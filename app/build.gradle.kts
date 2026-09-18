plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.ksp)
    alias(libs.plugins.room)
}

android {
    namespace = "club.hiraeth.flareenough"
    compileSdk = libs.versions.compileSdk.get().toInt()

    defaultConfig {
        applicationId = "club.hiraeth.flareenough"
        minSdk = libs.versions.minSdk.get().toInt()
        targetSdk = libs.versions.targetSdk.get().toInt()
        versionCode = 1
        versionName = "0.1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            // No shrinking yet. Turned on and tested in the release milestone.
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
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
