import org.jetbrains.kotlin.gradle.targets.js.nodejs.NodeJsRootExtension
import org.jetbrains.kotlin.gradle.targets.js.nodejs.NodeJsRootPlugin
import org.jetbrains.kotlin.gradle.targets.js.yarn.YarnPlugin
import org.jetbrains.kotlin.gradle.targets.js.yarn.YarnRootExtension

plugins {
    //trick: for the same plugin versions in all sub-modules
    // vanniktech maven-publish 0.30 需要 AGP ≥ 8.0（对齐 KuiklyEcho）
    id("com.android.application").version("8.3.2").apply(false)
    id("com.android.library").version("8.3.2").apply(false)
    kotlin("android").version("2.1.21").apply(false)
    kotlin("multiplatform").version("2.1.21").apply(false)
    id("com.google.devtools.ksp").version("2.1.21-2.0.1").apply(false)
    id("org.jetbrains.compose").version("1.7.3").apply(false)
    kotlin("plugin.compose").version("2.1.21").apply(false)
    // Maven Central 发布（KMP 多 target / 签名 / POM / 上传）
    id("com.vanniktech.maven.publish").version("0.30.0").apply(false)
    id("org.jetbrains.dokka").version("1.9.20").apply(false)
}

// H5：绕过 GitHub 拉 yarn/node（本机常超时）。Yarn 用本地 ivy 镜像；Node 用 PATH。
rootProject.plugins.withType(NodeJsRootPlugin::class.java) {
    extensions.configure(NodeJsRootExtension::class.java) {
        @Suppress("DEPRECATION")
        download = false
    }
}
rootProject.plugins.withType(YarnPlugin::class.java) {
    extensions.configure(YarnRootExtension::class.java) {
        val ivyBase = file("${System.getProperty("user.home")}/.gradle/caches/manual-yarn/ivy")
        @Suppress("DEPRECATION")
        downloadBaseUrl = ivyBase.toURI().toString().trimEnd('/')
    }
}

buildscript {
    repositories {
        maven {
            url = uri("https://mirrors.tencent.com/nexus/repository/maven-tencent/")
        }
        google()
        mavenCentral()
        mavenLocal()
        // AGP 7.4.2 自带 R8 4.0.52 无法 dex Kotlin 2.1 元数据（mergeExtDex → com.android.tools.r8.kotlin.H）
        maven {
            url = uri("https://storage.googleapis.com/r8-releases/raw")
        }
    }
    dependencies {
        classpath(BuildPlugin.kuikly)
        // Kotlin 2.1 最低要求 R8 8.6.17（见 Android Kotlin support 表）
        classpath("com.android.tools:r8:8.6.17")
    }
}