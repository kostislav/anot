package cz.judas.jan.jazyk2

import java.nio.file.Path
import kotlin.io.path.Path
import kotlin.io.path.createDirectories
import kotlin.io.path.div
import kotlin.io.path.listDirectoryEntries
import kotlin.io.path.name


class Compiler(private val backend: Backend) {
    private val frontend = Frontend()

    fun compile(sourceDir: Path) {
        val buildDir = (sourceDir / "build").createDirectories()
        val sourceFile = (sourceDir / "src").listDirectoryEntries().first()
        val packageAsts = frontend.process(sourceFile)
        backend.compile(packageAsts, buildDir, sourceDir.name)
    }
}


fun main() {
    val compiler = Compiler(GoBackend())
    compiler.compile(Path(""))
}