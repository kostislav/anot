package cz.judas.jan.jazyk2

import cz.judas.jan.jazyk2.ast.typed.Package
import java.nio.file.Path
import kotlin.io.path.reader

class Frontend {
    private val lexer = Lexer()
    private val parser = Parser()
    private val typer = Typer()

    fun process(filePackage: List<String>, sourceFile: Path): Package {
        return sourceFile.reader().use { reader ->
            val tokens = lexer.parseTokens(reader)
            val ast = parser.parseFile(tokens)
            typer.addTypeInfo(filePackage, ast)
        }
    }
}