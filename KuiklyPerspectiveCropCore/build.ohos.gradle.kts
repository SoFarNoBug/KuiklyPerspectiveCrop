plugins {
    kotlin("multiplatform")
    id("com.android.library")
    id("org.jetbrains.compose")
    kotlin("plugin.compose")
}

val mavenVersion: String = findProperty("mavenVersion") as? String
    ?: findProperty("MAVEN_VERSION") as? String
    ?: "1.0.0-2.30.0"
val groupId: String = findProperty("groupId") as? String
    ?: findProperty("GROUP_ID") as? String
    ?: "io.github.sofarnobug"

group = groupId
version = mavenVersion

kotlin {
    androidTarget {
        compilations.all {
            kotlinOptions {
                jvmTarget = "1.8"
            }
        }
        publishLibraryVariants("release")
    }

    ohosArm64 {
        val main by compilations.getting
        val ohosImage by main.cinterops.creating {
            defFile(project.file("src/ohosArm64Main/ohosInterop/cinterop/ohosImage.def"))
            includeDirs(project.file("src/ohosArm64Main/ohosInterop/include"))
        }
        val ohosDrawing by main.cinterops.creating {
            defFile(project.file("src/ohosArm64Main/ohosInterop/cinterop/ohosDrawing.def"))
            includeDirs(project.file("src/ohosArm64Main/ohosInterop/include"))
        }
    }

    sourceSets {
        val commonMain by getting {
            dependencies {
                api("com.tencent.kuikly-open:core:${Version.getKuiklyOhosVersion()}")
                api("com.tencent.kuikly-open:core-annotations:${Version.getKuiklyOhosVersion()}")
                api("com.tencent.kuikly-open:compose:${Version.getKuiklyOhosVersion()}")
                // Ohos KN 需要带 ohos_arm64 variant 的 KBA 坐标（官方 1.8.1 无该 target）
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.8.0-KBA-002")
            }
        }
        val androidMain by getting {
            dependencies {
                compileOnly("com.tencent.kuikly-open:core-render-android:${Version.getKuiklyOhosVersion()}")
            }
        }
        val ohosArm64Main by getting
    }
}

android {
    namespace = "com.jlj.kuiklybase.perspectivecrop"
    compileSdk = 34
    defaultConfig {
        minSdk = 21
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
}
