plugins {
    java
    application
    id("org.danilopianini.gradle-java-qa") version "1.25.0"
}

repositories {
    mavenCentral()
}

application {
    mainClass.set("it.unibo.mvc.DrawNumberApp")
}
