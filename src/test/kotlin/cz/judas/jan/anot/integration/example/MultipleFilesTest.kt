package cz.judas.jan.anot.integration.example

import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.nio.file.Path
import kotlin.io.path.Path

class MultipleFilesTest {
    @Test
    fun printsExpectedOutput(@TempDir workDir: Path) {
        val result = compileAndRun(Path("examples/projects/multiple_files"), workDir, "random")

        assertThat(result, equalTo("Hello from another file\n"))
    }
}