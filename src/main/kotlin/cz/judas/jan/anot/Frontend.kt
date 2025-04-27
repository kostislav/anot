package cz.judas.jan.anot

import cz.judas.jan.anot.ast.toSymbolMap
import cz.judas.jan.anot.ast.typed.FullyQualifiedName
import cz.judas.jan.anot.ast.typed.Package
import java.nio.file.Path
import kotlin.io.path.listDirectoryEntries
import kotlin.io.path.reader

class Frontend {
    private val lexer = Lexer()
    private val parser = Parser()
    private val typer = Typer()

    fun process(basePackage: FullyQualifiedName, sourceDir: Path): Package {
        // TODO subdirs
        val partiallyTypedFiles = sourceDir.listDirectoryEntries().map { sourceFile ->
            sourceFile.reader().use { reader ->
                val tokens = lexer.parseTokens(reader)
                val ast = parser.parseFile(tokens)
                typer.addSignatureTypeInfo(basePackage, ast)
            }
        }
        val symbolMap = partiallyTypedFiles.toSymbolMap() + Stdlib.symbolMap()
        return partiallyTypedFiles
            .map { sourceFile -> typer.addTypeInfo(sourceFile, symbolMap) }
            .reduce { first, second -> first + second }
    }
}