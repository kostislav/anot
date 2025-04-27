package cz.judas.jan.anot.integration.example

import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.nio.file.Path
import kotlin.io.path.Path
import kotlin.io.path.copyTo
import kotlin.io.path.createDirectories
import kotlin.io.path.createDirectory
import kotlin.io.path.div
import kotlin.io.path.writeText

class HelloTest {
    @Test
    fun printsExpectedOutput(@TempDir tempDir: Path) {
        val projectDir = tempDir / "project"
        val sourceDir = projectDir / "src"
        sourceDir.createDirectories()
        Path("examples/single_files/hello.anot").copyTo(sourceDir / "hello.anot")
        (projectDir / "project.yaml").writeText("""
            name: hello
            base_package: com.example.hello
            main: main
        """.trimIndent())
        val workDir = tempDir / "work"
        workDir.createDirectory()

        val result = compileAndRun(projectDir, workDir, "hello")

        assertThat(result, equalTo("Hello, world\n"))
    }
}