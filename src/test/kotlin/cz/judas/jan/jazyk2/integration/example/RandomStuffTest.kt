package cz.judas.jan.jazyk2.integration.example

import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.nio.file.Path
import kotlin.io.path.Path

class RandomStuffTest {
    @Test
    fun printsExpectedOutput(@TempDir workDir: Path) {
        val result = compileAndRun(Path("examples/random_stuff"), workDir, "random")

        assertThat(result, equalTo("Hello\nBig\nand\nWorld\n"))
    }
}