package cz.judas.jan.jazyk2

import kotlin.io.path.Path
import kotlin.io.path.createDirectories
import kotlin.io.path.div
import kotlin.io.path.writeLines


fun main() {
    val buildDir = Path("build")
    buildDir.createDirectories()

    val moduleFile = buildDir / "go.mod"
    moduleFile.writeLines(listOf(
        "module jazyk/main",
        "go 1.20"
    ))

    val goFile = buildDir / "main.go"
    goFile.writeLines(listOf(
        "package main",
        "func main() {",
        "  print(\"Hello, World!\\n\")",
        "}"
    ))
    val process = ProcessBuilder(listOf("go", "build", "-ldflags", "-s -w")).directory(buildDir.toFile()).start()
    process.waitFor()
}