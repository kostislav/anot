package cz.judas.jan.anot.integration.example

import cz.judas.jan.anot.Compiler
import cz.judas.jan.anot.CBackend
import java.nio.file.Path

fun compileAndRun(sourceDir: Path, workDir: Path, executableName: String): String {
    val compiler = Compiler(CBackend())

    compiler.compile(sourceDir, workDir)

    val process = ProcessBuilder("./${executableName}")
        .directory(workDir.toFile())
        .redirectOutput(ProcessBuilder.Redirect.PIPE)
        .start()
    process.waitFor()

    return process.inputStream.reader().readText()
}