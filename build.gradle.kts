plugins {
    val kotlinVersion = "1.7.0"
    kotlin("jvm") version kotlinVersion
    kotlin("plugin.serialization") version kotlinVersion

    id("net.mamoe.mirai-console") version "2.12.2"
}

group = "top.limbang"
version = "1.0.10"


repositories {
    maven("https://maven.aliyun.com/repository/public")
    mavenCentral()
}

dependencies {

    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.okhttp3:okhttp:5.0.0-alpha.2")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.4.0")
    compileOnly("top.limbang:mirai-plugin-general-interface:1.0.1")

    testImplementation(kotlin("test"))
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.4")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-debug:1.6.4")
    testImplementation( "com.squareup.okhttp3:logging-interceptor:5.0.0-alpha.2")
}