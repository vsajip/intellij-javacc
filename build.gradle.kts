@file:Suppress("PropertyName")

import org.intellij.lang.annotations.Language
import org.jetbrains.grammarkit.tasks.GenerateLexer
import org.jetbrains.grammarkit.tasks.GenerateParser


plugins {
    kotlin("jvm") version "1.3.10"
    id("java")
    id("org.jetbrains.intellij") version "0.3.12"
    id("org.jetbrains.grammarkit") version "2018.2.2"
}

val KotlinVersion by extra { "1.3.10" } // sync with above
val PackageRoot = "/com/github/oowekyala/ijcc"
val PathToPsiRoot = "$PackageRoot/lang/psi"


group = "com.github.oowekyala"
version = "1.0-BETA.2"


ext {
    // creates secret properties
    set("intellijPublishUsername", "")
    set("intellijPublishPassword", "")
    apply(from = "secrets.properties")
}



repositories {
    mavenCentral()
    jcenter()
}


dependencies {
    compile("org.jetbrains.kotlin:kotlin-stdlib-jdk8:$KotlinVersion")
    compile("org.apache.commons:commons-lang3:3.1") // only used to unescape java I think
    // https://mvnrepository.com/artifact/net.java.dev.javacc/javacc
    testCompile("com.github.oowekyala.treeutils:tree-matchers:2.0.1")
    testCompile("org.jetbrains.kotlin:kotlin-reflect:$KotlinVersion")
    testImplementation("io.kotlintest:kotlintest-runner-junit5:3.1.11")

    constraints {
        testImplementation("org.jetbrains.kotlin:kotlin-stdlib:$KotlinVersion")
        testImplementation("org.jetbrains.kotlin:kotlin-stdlib-jdk7:$KotlinVersion")
        testImplementation("org.jetbrains.kotlin:kotlin-reflect:$KotlinVersion")
    }

}

sourceSets {
    main {
        java {
            srcDirs("$buildDir/gen")
            srcDirs("src/main/kotlin")
            srcDirs("src/main/java")
        }
    }
}


intellij {
    version = "2018.2.4"
    updateSinceUntilBuild = false
}


tasks {

    test {
        useJUnitPlatform()
    }

    val GenerationTaskGroup = "Code generation"

    val generateParser by creating(GenerateParser::class) {
        group = GenerationTaskGroup
        description = "Generate the parser and PSI hierarchy"


        source = "src/main/grammars/JavaCC.bnf"
        targetRoot = "${buildDir}/gen"
        pathToParser = "/com/github/oowekyala/ijcc/lang/parser/JavaccParser.java"
        pathToPsiRoot = PathToPsiRoot
        purgeOldFiles = true
    }


    val generateLexer by creating(GenerateLexer::class) {
        group = GenerationTaskGroup
        description = "Generate the JFlex lexer used by the parser"

        source = "src/main/grammars/JavaCC.flex"
        targetDir = "${buildDir}/gen/com/github/oowekyala/ijcc/lang/lexer"
        targetClass = "JavaccLexer"
        purgeOldFiles = true
    }


    val overrideDefaultPsi: Task by creating {
        dependsOn(generateParser)

        group = GenerationTaskGroup
        description = "Eliminate the duplicate PSI classes found in the generated and main source tree"

        doLast {

            fun getPsiFiles(root: String): FileCollection {

                val psiInterfaces = "$root$PathToPsiRoot"
                val psiImpl = "$psiInterfaces/impl"

                return layout.files(file(psiInterfaces).listFiles()) + layout.files(file(psiImpl).listFiles())
            }


            val userPsiFiles = getPsiFiles("src/main/kotlin").map { it.nameWithoutExtension }

            val genPsiFileDups = getPsiFiles("$buildDir/gen").filter { genFile ->
                // in this source tree they're .java
                genFile.isFile && userPsiFiles.any { it == genFile.nameWithoutExtension }
            }
            logger.info("Detected ${genPsiFileDups.count()} generated PSI files overridden by sources in the main source tree:")
            genPsiFileDups.sorted().forEach { logger.info(it.name) }

            delete(genPsiFileDups)
            logger.info("Deleted.")
        }
    }

    compileJava {
        dependsOn(overrideDefaultPsi, generateLexer)
        sourceCompatibility = "1.8"
        targetCompatibility = "1.8"
    }

    compileKotlin {
        dependsOn(generateLexer, overrideDefaultPsi)

        kotlinOptions {
            freeCompilerArgs = listOf(
                "-Xjvm-default=enable",
                "-Xuse-experimental=kotlin.Experimental"
            )
            jvmTarget = "1.8"
        }

    }
    compileTestKotlin {
        kotlinOptions.jvmTarget = "1.8"
    }


    runIde {
        jvmArgs = listOf("-Xmx2G")
    }

    publishPlugin {
        username(project.property("intellijPublishUsername"))
        password(project.property("intellijPublishPassword"))
    }

    patchPluginXml {
        //language=HTML
        changeNotes(
            """
                Changelog since last release:
                <ul>
                <li>Improved JJTree support:
                    <ul>
                        <li>Documentation of productions features a link to the doc of the node class if it's found</li>
                        <li>Added gutter links to partial declarations of the node if they exist</li>
                        <li>
                            Rename refactoring now supports renaming JJTree nodes, taking care of all productions that generate it.
                        </li>
                    </ul>
                </li>
                <li>Other improvements:
                    <ul>
                        <li>Duplicate productions are now reported as an error</li>
                        <li>Added an intention to jump to the parser file if it's available</li>
                    </ul>
                </li>
                <li>Fixed some bugs:
                    <ul>
                        <li>Documentation for inline nodes (of the form "expansion #NodeName") is now resolved properly</li>
                        <li>Display name of some tokens in the structure view</li>
                        <li>Display name of option types</li>
                    </ul>
                </li>
                </ul>
            """.trimIndent()
        )

        version(project.version)
    }

}