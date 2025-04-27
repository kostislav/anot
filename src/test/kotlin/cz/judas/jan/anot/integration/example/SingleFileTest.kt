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

class SingleFileTest {
    @Test
    fun hello(@TempDir tempDir: Path) {
        assertThat(compileAndRun(tempDir, "hello.anot"), equalTo("Hello, world\n"))
    }

    @Test
    fun `return values`(@TempDir tempDir: Path) {
        assertThat(compileAndRun(tempDir, "return_values.anot"), equalTo("Hello returned world\n"))
    }

    private fun compileAndRun(tempDir: Path, fileName: String): String {
        val projectDir = tempDir / "project"
        val sourceDir = projectDir / "src"
        sourceDir.createDirectories()
        Path("examples/single_files/${fileName}").copyTo(sourceDir / fileName)
        (projectDir / "project.yaml").writeText(
            """
                name: example
                base_package: com.example.hello
                main: main
            """.trimIndent()
        )
        val workDir = tempDir / "work"
        workDir.createDirectory()

        val result = compileAndRun(projectDir, workDir, "example")
        return result
    }
}