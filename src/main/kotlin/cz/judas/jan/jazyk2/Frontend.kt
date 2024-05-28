package cz.judas.jan.jazyk2

import cz.judas.jan.jazyk2.ast.typed.Package
import java.nio.file.Path
import kotlin.io.path.reader

class Frontend {
    private val lexer = Lexer()
    private val parser = Parser()
    private val typer = Typer()

    fun process(filePackage: List<String>, sourceFiles: List<Path>): Package {
        val partiallyTypedFiles = sourceFiles.map { sourceFile ->
            sourceFile.reader().use { reader ->
                val tokens = lexer.parseTokens(reader)
                val ast = parser.parseFile(tokens)
                typer.addSignatureTypeInfo(filePackage, ast)
            }
        }
        val symbolMap = partiallyTypedFiles.fold(Stdlib.symbolMap()) { current, sourceFile -> current + sourceFile.symbolMap() }
        return partiallyTypedFiles
            .map { sourceFile -> typer.addTypeInfo(sourceFile, symbolMap) }
            .reduce { first, second -> first + second }
    }
}