package cz.judas.jan.jazyk2

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
    fun addTypeInfo(filePackage: List<String>, untypedSourceFile: UntypedSourceFile): TypedSourceFile {
        val importedSymbols = untypedSourceFile.imports
            .map { it.importedPath }
            .associate { it.last() to FullyQualifiedType(it) }

        val functions = untypedSourceFile.definitions
            .filterIsInstance<TopLevelDefinition.Function>()
            .map { resolve(it, filePackage, importedSymbols) }

        return TypedSourceFile(functions)
    }

    private fun resolve(untypedFunction: TopLevelDefinition.Function, filePackage: List<String>, importedSymbols: Map<String, FullyQualifiedType>): Function {
        return Function(
            untypedFunction.annotations.map { resolve(it, importedSymbols) },
            FullyQualifiedType(filePackage + untypedFunction.name),
            untypedFunction.body.map { resolve(it, importedSymbols) }
        )
    }

    private fun resolve(untypedAnnotation: UntypedAnnotation, importedSymbols: Map<String, FullyQualifiedType>): TypedAnnotation {
        return TypedAnnotation(importedSymbols.getValue(untypedAnnotation.type))
    }

    private fun resolve(untypedFunctionCall: UntypedExpression.FunctionCall, importedSymbols: Map<String, FullyQualifiedType>): TypedExpression.FunctionCall {
        return TypedExpression.FunctionCall(
            importedSymbols.getValue(untypedFunctionCall.functionName),
            untypedFunctionCall.arguments.map { resolve(it, importedSymbols) }
        )
    }

    private fun resolve(untypedExpression: UntypedExpression, importedSymbols: Map<String, FullyQualifiedType>): TypedExpression {
        return when (untypedExpression) {
            is UntypedExpression.StringConstant -> TypedExpression.StringConstant(untypedExpression.value)
            is UntypedExpression.FunctionCall -> resolve(untypedExpression, importedSymbols)
        }
    }
}