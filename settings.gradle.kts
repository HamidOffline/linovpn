pluginManagement {
    repositories {
        maven { url = java.net.URI("https://mirrors.tencent.com/nexus/repository/maven-public/") }
        maven { url = java.net.URI("https://maven.aliyun.com/repository/google") }
        maven { url = java.net.URI("https://maven.aliyun.com/repository/gradle-plugin") }
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        maven { url = java.net.URI("https://mirrors.tencent.com/nexus/repository/maven-public/") }
        maven { url = java.net.URI("https://maven.aliyun.com/repository/google") }
        google()
        mavenCentral()
        maven { url = java.net.URI("https://jitpack.io") }
    }
}

rootProject.name = "LinoVPN"
include(":app")
include(":core-common")
include(":core-xray")
include(":core-singbox")
include(":core-clash")
include(":core-v2ray")