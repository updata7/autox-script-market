plugins {
    alias(libs.plugins.autojs.android.library)
}
android {
    buildTypes {
        named("release") {
            isMinifyEnabled = false
            setProguardFiles(
                listOf(
                    getDefaultProguardFile("proguard-android.txt"),
                    "proguard-rules.pro"
                )
            )
        }
    }
    namespace = "com.stardust.autojs.apkbuilder"
    
    lint {
        abortOnError = false // 防止Lint错误中止构建
        baseline = file("lint-baseline.xml") // 创建基准文件
    }
}

dependencies {
    implementation(libs.core.ktx)

    implementation(libs.okhttp)

    api(files("libs/tiny-sign-0.9.jar"))

    api(libs.commons.io)

    androidTestImplementation(libs.androidx.test.ext.junit)
    androidTestImplementation(libs.espresso.core)
    testImplementation(libs.junit)
}
