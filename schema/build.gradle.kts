plugins {
    kotlin("jvm") version "1.3.10"
}

group = "xyz.luchengeng.spread"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation(kotlin("stdlib-jdk8"))
    implementation("com.google.code.gson:gson:2.8.6")
    implementation(project(":common"))
    implementation("com.google.re2j:re2j:1.3")
    implementation("org.apache.poi:poi:4.1.2")
    implementation("org.apache.poi:poi-ooxml:4.1.2")
}

tasks {
    compileKotlin {
        kotlinOptions.jvmTarget = "1.8"
    }
    compileTestKotlin {
        kotlinOptions.jvmTarget = "1.8"
    }
}