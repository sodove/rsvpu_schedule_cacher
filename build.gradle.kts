import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.5.10"
    application
}

group = "me.sodovaya"
version = "1.0"

repositories {
    mavenCentral()
}

dependencies{
    implementation("org.jsoup:jsoup:1.15.1")
    implementation("com.googlecode.json-simple:json-simple:1.1.1")
    implementation("com.squareup.okhttp3:okhttp:3.3.1")
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
}

application {
    mainClass.set("MainKt")
}