/*
 * This file was generated by the Gradle 'init' task.
 *
 * This generated file contains a sample Java project to get you started.
 * For more details take a look at the Java Quickstart chapter in the Gradle
 * user guide available at https://docs.gradle.org/5.1/userguide/tutorial_java_projects.html
 */

plugins {
    eclipse

    "java-library"
    // Apply the application plugin to add support for building an application
    application
    id("edu.sc.seis.version-class") version "1.2.2"
}

java {
    sourceCompatibility = JavaVersion.VERSION_1_10
    targetCompatibility = JavaVersion.VERSION_1_10
}

repositories {
    // Use jcenter for resolving your dependencies.
    // You can declare any Maven/Ivy/file repository here.
    mavenLocal()
    mavenCentral()
}

dependencies {
    implementation("edu.sc.seis:seedCodec:1.1.1")
    implementation("edu.sc.seis:seisFile:2.0.2")
    implementation("com.martiansoftware:jsap:2.1")
    //project(":seisFile")

    // Use JUnit Jupiter API for testing.
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.7.1")

    // Use JUnit Jupiter Engine for testing.
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.7.1")


}

group = "edu.sc.seis"
version = "0.1.0"

application {
    mainClass.set("edu.sc.seis.onesecminmax.DataLinkOneSec")
}
