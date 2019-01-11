/*
 * This file was generated by the Gradle 'init' task.
 *
 * This generated file contains a sample Java project to get you started.
 * For more details take a look at the Java Quickstart chapter in the Gradle
 * user guide available at https://docs.gradle.org/5.1/userguide/tutorial_java_projects.html
 */

plugins {
    eclipse

    // Apply the java plugin to add support for Java
    java

    // Apply the application plugin to add support for building an application
    application
}

repositories {
    // Use jcenter for resolving your dependencies.
    // You can declare any Maven/Ivy/file repository here.
    mavenLocal()
    jcenter()
}

dependencies {
    implementation("edu.sc.seis:seisFile:2.0.0-SNAPSHOT")
    //project(":seisFile")
    
    // Use JUnit test framework
    testImplementation("junit:junit:4.12")
}


application {
    // Define the main class for the application
    mainClassName = "edu.sc.seis.onesecminmax.DataLinkOneSec"
}
