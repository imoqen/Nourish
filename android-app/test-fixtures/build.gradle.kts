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

    kotlinOptions {
        jvmTarget = "11"
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)

    // the app:
    compileOnly(project(":app"))

    // http requests and responses:
    implementation("com.squareup.retrofit2:retrofit:3.0.0")

    // mocking retrofit:
    // https://central.sonatype.com/artifact/com.squareup.retrofit2/retrofit-mock
    // https://mvnrepository.com/artifact/com.squareup.retrofit2/retrofit-mock
    implementation("com.squareup.retrofit2:retrofit-mock:3.0.0")
}