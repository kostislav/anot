package cz.judas.jan.jazyk2

import java.nio.file.Path
import kotlin.io.path.Path
import kotlin.io.path.createDirectories
import kotlin.io.path.div
import kotlin.io.path.listDirectoryEntries
import kotlin.io.path.name


class Compiler(private val backend: Backend) {
    private val configReader = ProjectConfigReader()
    private val frontend = Frontend()

    fun compile(sourceDir: Path) {
        val projectConfig = configReader.forProject(sourceDir)
        val buildDir = (sourceDir / "build").createDirectories()
        val sourceFile = (sourceDir / "src").listDirectoryEntries().first()
        val packageAsts = frontend.process(projectConfig.basePackage, sourceFile)
        backend.compile(packageAsts, buildDir, sourceDir.name)
    }
}


fun main() {
    val compiler = Compiler(GoBackend())
    compiler.compile(Path("examples/hello"))
}