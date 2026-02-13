plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
}

android {
    namespace = "uk.co.imoqen.testfixtures"
    compileSdk = 36

    defaultConfig {
        minSdk = 33
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)

    // the app:
    implementation(project(":app"))
}