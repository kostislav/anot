package cz.judas.jan.jazyk2.integration.example

import cz.judas.jan.jazyk2.Compiler
import cz.judas.jan.jazyk2.GoBackend
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.nio.file.Path
import kotlin.io.path.Path

class HelloTest {
    @Test
    fun printsExpectedOutput(@TempDir workDir: Path) {
        val compiler = Compiler(GoBackend())

        compiler.compile(Path("examples/hello"), workDir)

        val process = ProcessBuilder("./hello")
            .directory(workDir.toFile())
            .redirectOutput(ProcessBuilder.Redirect.PIPE)
            .start()
        process.waitFor()
        val result = process.inputStream.reader().readText()

        assertThat(result, equalTo("Hello, world\n"))
    }
}