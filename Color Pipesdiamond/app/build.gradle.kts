plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.compose.compiler)
}

android {
    namespace = "com.color.pipe.puzzle"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.color.pipe.puzzle"
        minSdk = 26
        targetSdk = 36
        versionCode = 3
        versionName = "1.3"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    signingConfigs {
        create("release") {
            val keystoreFile = file("/Users/vijaykumar/AndroidDevelopment/Keystore/balaji.keystore")
            if (keystoreFile.exists()) {
                storeFile = keystoreFile
                storePassword = "android"
                keyAlias = "androidkey"
                keyPassword = "android"
            }
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            val releaseSigning = signingConfigs.getByName("release")
            if (releaseSigning.storeFile != null && releaseSigning.storeFile!!.exists()) {
                signingConfig = releaseSigning
            }
        }
    }
    buildFeatures {
        compose = true
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {
    implementation(libs.androidx.activity.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.core.ktx)
    implementation(libs.material)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.compose.foundation)
    implementation(libs.androidx.compose.animation)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.material.icons.extended)
    implementation(libs.androidx.compose.ui.text.google.fonts)
    implementation("com.airbnb.android:lottie-compose:6.0.0")
    // Anythink (Necessary)
    implementation("com.anythink.sdk:core-tpn:6.6.20.1")

    // Androidx (Necessary)
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("androidx.browser:browser:1.4.0")

    // Vungle
    implementation("com.anythink.sdk:adapter-tpn-vungle:7.6.1.1.1")
    implementation("com.vungle:vungle-ads:7.6.1")
    implementation("com.google.android.gms:play-services-basement:18.1.0")
    implementation("com.google.android.gms:play-services-ads-identifier:18.0.1")

    // UnityAds
    implementation("com.anythink.sdk:adapter-tpn-unityads:4.17.0.1.1")
    implementation("com.unity3d.ads:unity-ads:4.17.0")

    // Ironsource
    implementation("com.anythink.sdk:adapter-tpn-ironsource:9.2.0.1.1")
    implementation("com.unity3d.ads-mediation:mediation-sdk:9.2.0")
    implementation("com.google.android.gms:play-services-appset:16.0.2")

    // Facebook
    implementation("com.anythink.sdk:adapter-tpn-facebook:6.21.0.1.1")
    implementation("com.facebook.android:audience-network-sdk:6.21.0")
    implementation("androidx.annotation:annotation:1.0.0")

    // Inmobi
    implementation("com.anythink.sdk:adapter-tpn-inmobi:11.1.1.1.1")
    implementation("com.inmobi.monetization:inmobi-ads-kotlin:11.1.1")

    // Anythink Adx SDK(Necessary)
    implementation("com.anythink.sdk:adapter-tpn-sdm:6.5.72.1.0")
    implementation("com.smartdigimkttech.sdk:smartdigimkttech-sdk:6.5.72")

    // AppLovin
    implementation("com.anythink.sdk:adapter-tpn-applovin:13.6.0.1.1")
    implementation("com.applovin:applovin-sdk:13.6.0")

    // Mintegral
    implementation("com.anythink.sdk:adapter-tpn-mintegral:17.0.91.1.0")
    implementation("com.mbridge.msdk.oversea:mbridge_android_sdk:17.0.91")
    implementation("androidx.recyclerview:recyclerview:1.1.0")

    // Tramini
    implementation("com.anythink.sdk:tramini-plugin-tpn:6.6.20")

    // AdMob / Google Ad Manager
    implementation("com.anythink.sdk:adapter-tpn-admob:25.0.0.1.0")
    implementation("com.google.android.gms:play-services-ads:23.0.0")

    implementation(libs.okhttp)
    implementation(libs.gson)
    implementation(libs.onesignal)
    implementation(libs.ump)
    implementation(libs.installreferrer)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(platform(libs.androidx.compose.bom))
}

tasks.register<Exec>("extractLevels") {
    workingDir = file("../../")
    commandLine = listOf("python3", "extract_levels.py")
}