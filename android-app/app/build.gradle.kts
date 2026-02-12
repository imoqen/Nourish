import org.gradle.kotlin.dsl.invoke
import java.util.Locale

// app module build gradle file

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    // Google Cloud and Firebase connection: app-level. Also in root (nourish/build.gradle.kts)
    id("com.google.gms.google-services")

    // For navigation: https://developer.android.com/guide/navigation
    // Kotlin serialization plugin for type safe routes and navigation arguments.
    kotlin("plugin.serialization") version "2.0.21"

    // hilt:
    id("com.google.devtools.ksp")
    id("com.google.dagger.hilt.android")

    // JaCoCo to generate coverage reports for instrumented tests
    id("jacoco")
}

android {
    namespace = "uk.co.imoqen.nourish"
    compileSdk = 36

    defaultConfig {
        applicationId = "uk.co.imoqen.nourish"
        minSdk = 33
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        // Instrumentation test runner:
        testInstrumentationRunner = "uk.co.imoqen.nourish.NourishTestRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
        debug {
            enableUnitTestCoverage = true
            enableAndroidTestCoverage = true
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        compose = true
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    androidTestImplementation(libs.androidx.compose.ui.test.manifest)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)

    // Navigation:
    val nav_version = "2.9.6"
    implementation("androidx.navigation:navigation-compose:$nav_version")

    // ViewModel:
    val lifecycle_version = "2.10.0"
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:${lifecycle_version}")

    // Taken from: https://firebase.google.com/docs/android/setup At the end of this page,
    // it also specifies all firebase services which are available to use.

    // Google Firebase BOM:
    // "By using the Firebase Android BoM, your app will always use compatible Firebase library versions."
    // https://firebase.google.com/docs/android/learn-more
    // for firebase auth and firebase firestore.
    implementation(platform("com.google.firebase:firebase-bom:34.8.0"))

    // TODO: Add the dependencies for Firebase products you want to use
    // When using the BoM, don't specify versions in Firebase dependencies
    // https://firebase.google.com/docs/android/setup#available-libraries
    // For example, add the dependencies for Firebase Authentication and Cloud Firestore
    //implementation("com.google.firebase:firebase-auth")
    //implementation("com.google.firebase:firebase-firestore")

    // Firebase Authentication
    // When using BoM, you don't specify versions in Firebase library dependencies
    implementation("com.google.firebase:firebase-auth")

    // Firebase Google account authentication (for logins with Google):
    // also with dependencies for the Credential Manager libraries and specify their versions
    implementation("androidx.credentials:credentials:1.3.0")
    implementation("androidx.credentials:credentials-play-services-auth:1.3.0")
    implementation("com.google.android.libraries.identity.googleid:googleid:1.1.1")

    // For HTTP requests and responses. retrofit: https://github.com/square/retrofit
    implementation("com.squareup.retrofit2:retrofit:3.0.0")
    // json converter
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")

    // Firebase Firestore (database):
    // Declare the dependency for the Cloud Firestore library
    // When using the BoM, you don't specify versions in Firebase library dependencies
    implementation("com.google.firebase:firebase-firestore")

    // hilt:
    implementation("com.google.dagger:hilt-android:2.57.1")
    ksp("com.google.dagger:hilt-compiler:2.57.1")
    implementation("androidx.hilt:hilt-navigation-compose:1.2.0")

    // Hilt testing:
    androidTestImplementation("com.google.dagger:hilt-android-testing:2.57.1")
    kspAndroidTest("com.google.dagger:hilt-android-compiler:2.57.1")
}

// For generating coverage report: Jacoco
// https://docs.gradle.org/current/userguide/jacoco_plugin.html
// code adapted from: https://medium.com/make-android/measure-your-codes-reach-integrating-jacoco-code-coverage-in-android-apps-with-kotlin-dsl-382d577864e7
// coverage reports are generated in app/build/customJacocoReportDir/JacocoCodeCoverage

jacoco {
    toolVersion = "0.8.14"
    reportsDirectory = layout.buildDirectory.dir("customJacocoReportDir")
}

val exclusions = listOf(
    "**/R.class",
    "**/R\$*.class",
    "**/BuildConfig.*",
    "**/Manifest*.*",
    "**/*Test*.*",
    "**/hilt_aggregated_deps/**",
    "**/dagger/hilt/internal/aggregatedroot/codegen/**",
    "**/dagger/**"
)

tasks.withType(Test::class) {
    configure<JacocoTaskExtension> {
        isIncludeNoLocationClasses = true
        excludes = listOf("jdk.internal.*")
    }
}

// Register a JacocoReport task for code coverage analysis
tasks.register<JacocoReport>("JacocoCodeCoverage") {
    // Depend on unit tests and Android tests tasks
    dependsOn("connectedDebugAndroidTest")
    // Set task grouping and description
    group = "Reporting"
    description = "Execute UI and unit tests, generate and combine Jacoco coverage report"
    // Configure reports to generate both XML and HTML formats
    reports {
        xml.required.set(false)
        html.required.set(true)
    }
    // Set source directories to the main source directory
    sourceDirectories.setFrom(layout.projectDirectory.dir("src/main"))
    // Set class directories to compiled Java and Kotlin classes, excluding specified exclusions
    classDirectories.setFrom(
        files(
        fileTree(layout.buildDirectory.dir("intermediates/javac/")) {
            exclude(exclusions)
        },
        fileTree(layout.buildDirectory.dir("tmp/kotlin-classes/")) {
            exclude(exclusions)
        }
    ))
    // Collect execution data from .exec and .ec files generated during test execution
    executionData.setFrom(
        files(
        fileTree(layout.buildDirectory) { include(listOf("**/*.exec", "**/*.ec")) }
    ))
}