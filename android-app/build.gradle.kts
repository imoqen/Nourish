// app root build gradle file

// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.compose) apply false
    // Google Cloud and Firebase connection: root level. There is also another within /app/build.gradle.kts
    id("com.google.gms.google-services") version "4.4.4" apply false

    // hilt:
    id("com.google.dagger.hilt.android") version "2.57.1" apply false
    id("com.google.devtools.ksp") version "2.0.21-1.0.25" apply false
}