plugins {
    id("java")
}

repositories {
    mavenCentral()
    maven {
        name = "WildCommons"
        url = uri("https://maven.pkg.github.com/danib150/WildCommons")

        credentials {
            username = providers.gradleProperty("gpr.user").orNull
                ?: System.getenv("GITHUB_ACTOR")
            password = providers.gradleProperty("gpr.key").orNull
                ?: System.getenv("GITHUB_TOKEN")
        }
    }

    maven("https://hub.spigotmc.org/nexus/content/repositories/snapshots/")

}

dependencies {
    compileOnly("net.md-5:bungeecord-chat:1.21-R0.4")
    compileOnly("org.spigotmc:spigot-api:1.8.8-R0.1-SNAPSHOT")
    compileOnly("it.danielebruni.wildadventure:wildcommons-core:1.0.1")
    compileOnly("org.projectlombok:lombok:1.18.44")
    annotationProcessor("org.projectlombok:lombok:1.18.44")

}