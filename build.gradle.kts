plugins {
    val kotlinVersion = "2.3.0"
    kotlin("jvm") version kotlinVersion
    kotlin("plugin.serialization") version kotlinVersion

    id("net.mamoe.mirai-console") version "2.16.0"
}

group = "top.limbang"
version = "1.2.0"


repositories {
    maven("https://maven.aliyun.com/repository/public")
    mavenCentral()
}

dependencies {

    implementation("org.slf4j:slf4j-api:2.0.17")
    implementation("com.squareup.retrofit2:retrofit:3.0.0")
    implementation("com.squareup.okhttp3:okhttp:5.3.2")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.9.0")
    compileOnly("top.limbang:mirai-plugin-general-interface:1.0.2")

    testImplementation(kotlin("test"))
    testImplementation("ch.qos.logback:logback-classic:1.5.25")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.10.2")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-debug:1.10.2")
    testImplementation("com.squareup.okhttp3:logging-interceptor:5.3.2")
}