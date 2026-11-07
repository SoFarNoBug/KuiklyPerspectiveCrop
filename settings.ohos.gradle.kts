pluginManagement {
    repositories {
        google()
        gradlePluginPortal()
        mavenCentral()
        mavenLocal()
        maven {
            url = uri("https://mirrors.tencent.com/nexus/repository/maven-tencent/")
        }
    }
}

dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
        mavenLocal()
        maven {
            url = uri("https://mirrors.tencent.com/nexus/repository/maven-tencent/")
        }
    }
}

rootProject.name = "KuiklyPerspectiveCropUI"

val buildFileName = "build.ohos.gradle.kts"
rootProject.buildFileName = buildFileName

include(":KuiklyPerspectiveCropCore")
project(":KuiklyPerspectiveCropCore").buildFileName = buildFileName
include(":perspectiveCropDemo")
project(":perspectiveCropDemo").buildFileName = buildFileName
