package cz.judas.jan.anot

import cz.judas.jan.anot.ast.typed.FullyQualifiedName
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
        val sourceFiles = (sourceDir / "src").listDirectoryEntries() // TODO subdirs
        val packageAsts = frontend.process(FullyQualifiedName(projectConfig.basePackage), sourceFiles)
        backend.compile(packageAsts, buildDir, FullyQualifiedName(projectConfig.basePackage + projectConfig.mainFunctionName), projectConfig.name)
    }
}


fun main() {
    val compiler = Compiler(CBackend())
    compiler.compile(Path("examples/random_stuff"))
}