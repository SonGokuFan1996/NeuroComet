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
        // Required for Stream WebRTC Android
        maven { url = uri("https://jitpack.io") }
        maven { url = uri("https://maven.getstream.io/repo/release") }
    }
}

rootProject.name = "NeuroComet"
include(":app")