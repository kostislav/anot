package cz.judas.jan.jazyk2.integration.example

import org.hamcrest.MatcherAssert
import org.hamcrest.Matchers
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.nio.file.Path
import kotlin.io.path.Path

class RandomStuffTest {
    @Test
    fun printsExpectedOutput(@TempDir workDir: Path) {
        val result = compileAndRun(Path("examples/random_stuff"), workDir, "random")

        MatcherAssert.assertThat(result, Matchers.equalTo("Hello\nWorld\n"))
    }
}