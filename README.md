# SafeWalk App

## Build Fixes

### 1. Removed unavailable dependency

The build was failing because it couldn't find `androidx.sensors:sensors-core:1.0.0-alpha05`. 
This dependency was removed from the build.gradle.kts file as it's not available in the repositories
and isn't actually needed since the app directly uses Android's built-in sensor APIs.

### 2. Added Google Maps API Key

The build was failing because the AndroidManifest.xml was using a placeholder `${MAPS_API_KEY}` without providing an actual value. 
A Google Maps API key has been added to the build.gradle.kts file:

```kotlin
defaultConfig {
    // Other configurations...
    
    // Use the real Google Maps API key
    manifestPlaceholders["MAPS_API_KEY"] = "AIzaSyDncP_8tLKG1_LQISCOji4iFP4BzGFfHo4"
}
```

### 3. Fixed build.gradle.kts file

The build.gradle.kts file had issues with the code trying to load properties from local.properties. This was causing 
compilation errors with "Unresolved reference" messages. The build.gradle.kts file has been simplified to directly set
the Maps API key without trying to load it from local.properties.

### 4. Fixed Firebase dependency conflict

There was a conflict between different versions of Firebase libraries that contained duplicate classes.
This was resolved by adding a resolution strategy to force a consistent version:

```kotlin
configurations.all {
    resolutionStrategy {
        // Force all Firebase dependencies to use the same version
        force("com.google.firebase:firebase-common:20.3.3")
    }
}
```

### 5. Fixed Kotlin compilation errors

Several Kotlin files had compilation errors:

1. Missing imports in Navigation.kt for Compose Navigation components.
2. Type mismatch in ContactsScreen.kt when using the ContactsState.Error class.
3. Missing extension functions for Result class in SafetyViewModel.kt.

These issues were fixed by adding the proper imports and implementing the necessary extension functions.

## Compose Compiler Configuration

Starting in Kotlin 2.0, the Compose Compiler Gradle plugin is required when compose is enabled. The following changes have been made to the project:

1. Added the Compose Compiler plugin to the plugins block in `app/build.gradle.kts`:
```kotlin
plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    id("com.google.gms.google-services")
    alias(libs.plugins.kotlin.compose)
}
```

2. Updated `gradle/libs.versions.toml` to include the compose-compiler version and plugin:
```toml
[versions]
# Other versions...
compose-compiler = "1.5.8"

[plugins]
# Other plugins...
kotlin-compose = { id = "org.jetbrains.kotlin.plugin.compose", version.ref = "kotlin" }
```

3. Updated the composeOptions in `app/build.gradle.kts` to use the referenced version:
```kotlin
composeOptions {
    kotlinCompilerExtensionVersion = libs.versions.compose.compiler.get()
}
```

This configuration ensures compatibility with Kotlin 2.0 and the Compose compiler.

## References

For more information, see: https://d.android.com/r/studio-ui/compose-compiler 