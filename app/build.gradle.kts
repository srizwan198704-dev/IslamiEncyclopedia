import com.android.build.gradle.internal.cxx.configure.gradleLocalProperties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.jetbrains.kotlin.android)
    id("org.jetbrains.kotlin.kapt")
}

// Local properties থেকে কী-গুলো রিড করা
val api = gradleLocalProperties(rootDir, providers).getProperty("api", "")
val tafsir = gradleLocalProperties(rootDir, providers).getProperty("tafsir", "")
val text = gradleLocalProperties(rootDir, providers).getProperty("text", "")
val pdf = gradleLocalProperties(rootDir, providers).getProperty("pdf", "")
val boyan = gradleLocalProperties(rootDir, providers).getProperty("boyan", "")
val blogid = gradleLocalProperties(rootDir, providers).getProperty("blogid", "")
val api2 = gradleLocalProperties(rootDir, providers).getProperty("api2", "")
val textapi = gradleLocalProperties(rootDir, providers).getProperty("textapi", "")

android {
    namespace = "com.srizwan.islamipedia"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.srizwan.islamipedia"
        minSdk = 21
        targetSdk = 36
        versionCode = 7
        versionName = "7.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        val keys = mapOf(
            "api" to api, "tafsir" to tafsir, "text" to text, "pdf" to pdf,
            "boyan" to boyan, "blogid" to blogid, "api2" to api2, "textapi" to textapi
        )
        keys.forEach { (key, value) ->
            resValue("string", key, "\"$value\"")
        }
    }

    signingConfigs {
        create("release") {
            storeFile = file("book.jks")
            storePassword = "book0102"
            keyAlias = "book"
            keyPassword = "book0102"

            enableV1Signing = true
            enableV2Signing = true
            enableV3Signing = true
            enableV4Signing = true
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            signingConfig = signingConfigs.getByName("release")

            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )

            val keys = listOf("api", "tafsir", "text", "pdf", "boyan", "blogid", "api2", "textapi")
            keys.forEach { key ->
                val value = gradleLocalProperties(rootDir, providers).getProperty(key, "")
                buildConfigField("String", key, "\"$value\"")
            }
        }

        debug {
            val keys = listOf("api", "tafsir", "text", "pdf", "boyan", "blogid", "api2", "textapi")
            keys.forEach { key ->
                val value = gradleLocalProperties(rootDir, providers).getProperty(key, "")
                buildConfigField("String", key, "\"$value\"")
            }
        }
    }

    packaging {
        jniLibs {
            useLegacyPackaging = false
        }
    }

    // ✅ FIXED: Java 17 (stable for CI + KAPT)
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    // ✅ FIXED: Kotlin JVM 17
    kotlin {
        compilerOptions {
            jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
        }
    }

    buildFeatures {
        buildConfig = true
    }

    buildToolsVersion = "36.1.0"
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)

    implementation("com.github.DImuthuUpe:AndroidPdfViewer:3.1.0-beta.1")
    implementation("com.google.code.gson:gson:2.10.1")
    implementation("com.github.amitshekhariitbhu:PRDownloader:1.0.2")
    implementation("com.github.bumptech.glide:glide:4.12.0")
    implementation("com.squareup.okhttp3:okhttp:4.12.0")

    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.9.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.9.0")

    implementation("androidx.swiperefreshlayout:swiperefreshlayout:1.1.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.6.2")
    implementation("androidx.webkit:webkit:1.7.0")

    implementation("com.google.android.gms:play-services-location:21.3.0")
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation("com.jakewharton.threetenabp:threetenabp:1.4.0")
    implementation("androidx.recyclerview:recyclerview:1.3.2")
    implementation("com.squareup.okhttp:okhttp:2.7.5")

    implementation("com.google.ai.client.generativeai:generativeai:0.9.0")
    implementation("com.batoulapps.adhan:adhan2:0.0.5")
    implementation("com.pierfrancescosoffritti.androidyoutubeplayer:core:12.1.1")
    implementation("com.batoulapps.adhan:adhan:1.2.1")
    implementation("org.jsoup:jsoup:1.16.1")

    // Room
    implementation("androidx.room:room-common:2.6.1")
    implementation("androidx.room:room-runtime:2.6.1")
    implementation("androidx.room:room-ktx:2.6.1")
    kapt("androidx.room:room-compiler:2.6.1")

    // Paging
    implementation("androidx.paging:paging-runtime-ktx:3.2.1")

    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}

// ✅ FINAL KAPT FIX (CI stable)
kapt {
    correctErrorTypes = true
    useBuildCache = true
}
