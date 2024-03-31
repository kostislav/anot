package cz.judas.jan.jazyk2

import java.nio.file.Path
import kotlin.io.path.Path
import kotlin.io.path.createDirectories
import kotlin.io.path.div
import kotlin.io.path.listDirectoryEntries


class Compiler(private val backend: Backend) {
    private val configReader = ProjectConfigReader()
    private val frontend = Frontend()

    fun compile(sourceDir: Path, buildDir: Path = sourceDir  / "build") {
        val projectConfig = configReader.forProject(sourceDir)
        buildDir.createDirectories()
        val sourceFile = (sourceDir / "src").listDirectoryEntries().first()
        val packageAsts = frontend.process(projectConfig.basePackage, sourceFile)
        backend.compile(packageAsts, buildDir, projectConfig.name)
    }
}


fun main() {
    val compiler = Compiler(GoBackend())
    compiler.compile(Path("examples/random_stuff"))
}