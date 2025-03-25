plugins {
    java
    alias(libs.plugins.pluginYmlPaper)
    alias(libs.plugins.paperweightUserdev)
}

group = "io.github.tanguygab"
version = "1.0.2"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

repositories {
    mavenCentral()
    maven {
        url = uri("https://repo.papermc.io/repository/maven-public/")
    }
}

dependencies {
	compileOnly(libs.paperApi)
    paperweightDevelopmentBundle(libs.paperweightBundle)
}

paper {
    main = "io.github.tanguygab.petnamefix.PetNameFix"
    apiVersion = libs.versions.paper.get().replace(Regex("\\-R\\d.\\d-SNAPSHOT"), "")
    authors = listOf("NEZNAMY", "Tanguygab", "Jim (AnEnragedPigeon)")
    description = "A feature to disable minecraft feature making tamed animals with custom names copy NameTag properties of their owner."
}

tasks {
    compileJava {
        options.compilerArgs.addAll(listOf("-Xlint:all", "-Xlint:-processing"))
        options.encoding = "UTF-8"
    }
}
