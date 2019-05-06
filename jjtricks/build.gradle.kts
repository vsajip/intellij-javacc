import com.github.oowekyala.includeIjCoreDeps
import com.github.oowekyala.includeJars
import com.github.oowekyala.intellijCoreDep
import com.github.oowekyala.intellijDep

plugins {
    kotlin("jvm")
    id("java")
    // Applying the grammarkit plugin allows resolving the grammarkit dependency
    // bequested by the :core project... Idk there may be some repo stuff going
    // on
    id("org.jetbrains.grammarkit") version "2018.2.2"
    id("com.github.johnrengelman.shadow") version "5.0.0"
}

group = "com.github.oowekyala"
version = "1.0"

val runtime by configurations
val compileOnly by configurations


dependencies {
    api(project(":core"))

    // TODO make compileOnly for the IDE plugin to depend on it
    compile(intellijCoreDep()) { includeJars("intellij-core") }
    compile(intellijDep()) {
        includeIjCoreDeps(rootProject)
        includeJars("platform-api")
    }

    // this is for jjtx
    api("com.google.guava:guava:27.0.1-jre")
    api("org.apache.velocity:velocity:1.6.2")

    implementation("com.google.code.gson:gson:2.8.5")
    implementation("com.github.oowekyala.treeutils:tree-printers:2.0.2")
    implementation("org.yaml:snakeyaml:1.24")
    implementation("com.google.googlejavaformat:google-java-format:1.7")
    // for debugging only, this pulls in a huge IBM dependency
    // implementation("com.tylerthrailkill.helpers:pretty-print:2.0.1")
    implementation("com.xenomachina:kotlin-argparser:2.0.7")

    testImplementation(project(":core").dependencyProject.sourceSets["test"].output)
}

sourceSets {
    main {
        java {
            srcDirs("src/main/kotlin")
            srcDirs("src/main/java")
        }
    }
}



tasks {

    compileJava {}

    compileKotlin {
        kotlinOptions {
            freeCompilerArgs = listOf(
                "-Xjvm-default=enable",
                "-Xuse-experimental=kotlin.Experimental"
            )
            jvmTarget = "1.8"
        }

    }

    shadowJar {
        baseName = "jjtricks-min"
        appendix = ""


        mergeServiceFiles()

        minimize {
            exclude(dependency("org.apache.velocity:velocity:.*"))
            exclude(dependency("org.jetbrains.kotlin:.*:.*"))
            exclude(dependency("com.google.googlejavaformat:.*:.*"))
            exclude(dependency("com.google.guava:.*:.*"))
            exclude(dependency("com.google.guava:.*:.*"))
            exclude(dependency("com.google.errorprone:javac-shaded:.*"))
        }

        manifest {
            attributes(
                "Main-Class" to "com.github.oowekyala.jjtx.Jjtricks"
            )
        }
    }
}
