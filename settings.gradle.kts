pluginManagement {
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

// Flare Enough. A free, offline, open source medication, symptom and stillness app.
rootProject.name = "FlareEnough"

// The Android application.
include(":app")

// Pure Kotlin module holding the daylight saving safe reminder scheduling logic.
// It has no Android dependencies so its tests run on any plain JVM.
include(":schedule")
