package cz.judas.jan.anot

import org.yaml.snakeyaml.Yaml
import java.nio.file.Path
import kotlin.io.path.div
import kotlin.io.path.reader

class ProjectConfigReader {
    fun forProject(projectDir: Path): ProjectConfig {
        val reader = Yaml()
        val contents = (projectDir / "project.yaml").reader().use { reader.load<Map<String, String>>(it) }

        return ProjectConfig(
            contents.getValue("name"),
            contents.getValue("base_package").split("."),
            contents.getValue("main"),
        )
    }
}

data class ProjectConfig(
    val name: String,
    val basePackage: List<String>,
    val mainFunctionName: String,
)