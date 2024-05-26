package cz.judas.jan.jazyk2

import cz.judas.jan.jazyk2.ast.FunctionSignature
import cz.judas.jan.jazyk2.ast.PartiallyTypedFunction
import cz.judas.jan.jazyk2.ast.PartiallyTypedSourceFile
import cz.judas.jan.jazyk2.ast.typed.FullyQualifiedType
import cz.judas.jan.jazyk2.ast.typed.Function
import cz.judas.jan.jazyk2.ast.untyped.TopLevelDefinition
import cz.judas.jan.jazyk2.ast.typed.Annotation as TypedAnnotation
import cz.judas.jan.jazyk2.ast.typed.Expression as TypedExpression
import cz.judas.jan.jazyk2.ast.typed.Package as TypedSourceFile
import cz.judas.jan.jazyk2.ast.untyped.Annotation as UntypedAnnotation
import cz.judas.jan.jazyk2.ast.untyped.Expression as UntypedExpression
import cz.judas.jan.jazyk2.ast.untyped.SourceFile as UntypedSourceFile

class Typer {
    fun addSignatureTypeInfo(filePackage: List<String>, untypedSourceFile: UntypedSourceFile): PartiallyTypedSourceFile {
        val importedSymbols = untypedSourceFile.imports
            .map { it.importedPath }
            .associate { it.last() to FullyQualifiedType(it) }
        val localDefinitions = untypedSourceFile.definitions
            .associate { it.name to FullyQualifiedType(filePackage + it.name) }
        val symbols = importedSymbols + localDefinitions

        val functions = untypedSourceFile.definitions
            .filterIsInstance<TopLevelDefinition.Function>()
            .map { PartiallyTypedFunction(it.annotations, resolveSignature(it, symbols), it.body) }

        return PartiallyTypedSourceFile(
            filePackage,
            symbols,
            functions,
        )
    }

    fun addTypeInfo(sourceFile: PartiallyTypedSourceFile, symbolMap: SymbolMap): TypedSourceFile {
        val functions = sourceFile.functions
            .map { resolve(it, sourceFile.filePackage, sourceFile.imports, symbolMap) }

        return TypedSourceFile(functions)
    }

    private fun resolveSignature(untypedFunction: TopLevelDefinition.Function, importedSymbols: Map<String, FullyQualifiedType>): FunctionSignature {
        return FunctionSignature(
            untypedFunction.name,
            untypedFunction.returnType?.let { importedSymbols.getValue(it) } ?: Stdlib.void,
        )
    }

    private fun resolve(
        partiallyTypedFunction: PartiallyTypedFunction,
        filePackage: List<String>,
        importedSymbols: Map<String, FullyQualifiedType>,
        symbolMap: SymbolMap,
    ): Function {
        return Function(
            partiallyTypedFunction.annotations.map { resolve(it, importedSymbols) },
            FullyQualifiedType(filePackage + partiallyTypedFunction.signature.name),
            partiallyTypedFunction.signature.returnType,
            partiallyTypedFunction.body.map { resolve(it, importedSymbols, symbolMap) }
        )
    }

    private fun resolve(untypedAnnotation: UntypedAnnotation, importedSymbols: Map<String, FullyQualifiedType>): TypedAnnotation {
        return TypedAnnotation(importedSymbols.getValue(untypedAnnotation.type))
    }

    private fun resolve(
        untypedFunctionCall: UntypedExpression.FunctionCall,
        importedSymbols: Map<String, FullyQualifiedType>,
        symbolMap: SymbolMap,
    ): TypedExpression.FunctionCall {
        val function = importedSymbols.getValue(untypedFunctionCall.functionName)
        return TypedExpression.FunctionCall(
            function,
            untypedFunctionCall.arguments.map { resolve(it, importedSymbols, symbolMap) },
            symbolMap.functions.getValue(function).returnType,
        )
    }

    private fun resolve(untypedExpression: UntypedExpression, importedSymbols: Map<String, FullyQualifiedType>, symbolMap: SymbolMap): TypedExpression {
        return when (untypedExpression) {
            is UntypedExpression.StringConstant -> TypedExpression.StringConstant(untypedExpression.value)
            is UntypedExpression.FunctionCall -> resolve(untypedExpression, importedSymbols, symbolMap)
        }
    }
}