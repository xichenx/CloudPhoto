import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
}

kotlin {
    androidTarget {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_11)
        }
    }
    
    sourceSets {
        androidMain.dependencies {
            implementation(compose.preview)
            implementation(libs.androidx.activity.compose)
            implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.9.0")
            implementation("androidx.lifecycle:lifecycle-runtime-compose:2.9.0")
            implementation("androidx.core:core-ktx:1.17.0")
            
            // Ktor：OkHttp 引擎 + 磁盘缓存（Caches 目录、LRU，非业务持久化相册）
            implementation("io.ktor:ktor-client-okhttp:2.3.12")
            implementation("io.ktor:ktor-client-content-negotiation:2.3.12")
            implementation("io.ktor:ktor-serialization-kotlinx-json:2.3.12")
            implementation("io.ktor:ktor-client-logging:2.3.12")
            
            // Material Icons Extended
            implementation("androidx.compose.material:material-icons-extended:1.7.1")
            
            // Navigation Compose
            implementation("androidx.navigation:navigation-compose:2.8.4")
            
            // System UI Controller
            implementation("com.google.accompanist:accompanist-systemuicontroller:0.34.0")
        }
        commonMain.dependencies {
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material3)
            implementation(compose.ui)
            implementation(compose.components.resources)
            implementation(compose.components.uiToolingPreview)
            implementation(libs.androidx.lifecycle.viewmodelCompose)
            implementation(libs.androidx.lifecycle.runtimeCompose)
            implementation(projects.shared)
            
            // DateTime
            implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.6.1")
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }
    }
}

android {
    namespace = "com.xichen.cloudphoto"
    compileSdk = libs.versions.android.compileSdk.get().toInt()

    defaultConfig {
        applicationId = "com.xichen.cloudphoto"
        minSdk = libs.versions.android.minSdk.get().toInt()
        targetSdk = libs.versions.android.targetSdk.get().toInt()
        versionCode = 1
        versionName = "1.0"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {
    debugImplementation(compose.uiTooling)
}

