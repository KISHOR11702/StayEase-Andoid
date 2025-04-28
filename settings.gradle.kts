pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal() // ✅ Ensure plugin resolution
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "StayEaseApp"
include(":app") // ✅ Ensures 'app' module is included
