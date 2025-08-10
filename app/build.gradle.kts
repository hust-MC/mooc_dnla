plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
}

android {
    namespace = "com.max.dlna"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.max.dlna"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    //--------- 以上为系统自动添加，不做要求，自行处理

    // cling相关依赖
    implementation("org.fourthline.cling:cling-core:2.1.2")
    implementation("org.fourthline.cling:cling-support:2.1.2")

    // cling依赖的其他库
    implementation("org.eclipse.jetty:jetty-servlet:8.1.22.v20160922")
    implementation("org.eclipse.jetty:jetty-server:8.1.22.v20160922")
    implementation("org.eclipse.jetty:jetty-client:8.1.22.v20160922")

    implementation("javax.servlet:javax.servlet-api:3.1.0")

}