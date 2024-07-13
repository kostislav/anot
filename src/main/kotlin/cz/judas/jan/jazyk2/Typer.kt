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
            .map { if (it.isAbsolute) it.importedPath else filePackage + it.importedPath }
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
            .map { resolveFunction(it, sourceFile.filePackage, Scope.topLevel(sourceFile.imports, symbolMap)) }

        return TypedSourceFile(functions)
    }

    private fun resolveSignature(untypedFunction: TopLevelDefinition.Function, importedSymbols: Map<String, FullyQualifiedType>): FunctionSignature {
        return FunctionSignature(
            untypedFunction.name,
            untypedFunction.returnType?.let { importedSymbols.getValue(it) } ?: Stdlib.void,
        )
    }

    private fun resolveFunction(
        partiallyTypedFunction: PartiallyTypedFunction,
        filePackage: List<String>,
        scope: Scope,
    ): Function {
        return Function(
            partiallyTypedFunction.annotations.map { resolveAnnotation(it, scope) },
            FullyQualifiedType(filePackage + partiallyTypedFunction.signature.name),
            partiallyTypedFunction.signature.returnType,
            partiallyTypedFunction.body.map { resolveExpression(it, scope) }
        )
    }

    private fun resolveAnnotation(untypedAnnotation: UntypedAnnotation, scope: Scope): TypedAnnotation {
        return TypedAnnotation(scope.getClass(untypedAnnotation.type).type)
    }

    private fun resolveFunctionCall(
        untypedFunctionCall: UntypedExpression.FunctionCall,
        scope: Scope,
    ): TypedExpression.FunctionCall {
        val function = scope.getFunction(untypedFunctionCall.functionName)
        return TypedExpression.FunctionCall(
            function.type,
            untypedFunctionCall.arguments.map { resolveExpression(it, scope) },
            function.signature.returnType,
        )
    }

    private fun resolveExpression(untypedExpression: UntypedExpression, scope: Scope): TypedExpression {
        return when (untypedExpression) {
            is UntypedExpression.StringConstant -> TypedExpression.StringConstant(untypedExpression.value)
            is UntypedExpression.FunctionCall -> resolveFunctionCall(untypedExpression, scope)
        }
    }

    private class Scope(
        private val symbols: Map<String, Entry>,
    ) {
        sealed interface Entry {
            data class Function(val type: FullyQualifiedType, val signature: FunctionSignature): Entry
            data class Class(val type: FullyQualifiedType): Entry
        }

        fun getClass(name: String): Entry.Class {
            return symbols.getValue(name) as Entry.Class
        }

        fun getFunction(name: String): Entry.Function {
            return symbols.getValue(name) as Entry.Function
        }

        companion object {
            fun topLevel(imports: Map<String, FullyQualifiedType>, symbolMap: SymbolMap): Scope {
                return Scope(
                    imports.mapValues { (_, type) -> resolve(type, symbolMap) }
                )
            }

            private fun resolve(type: FullyQualifiedType, symbolMap: SymbolMap): Entry {
                val function = symbolMap.functions[type]
                return if (function !== null) {
                    Entry.Function(type, function)
                } else if (type in symbolMap.classes) {
                    Entry.Class(type)
                } else {
                    throw RuntimeException("No symbol for type ${type}")
                }
            }
        }
    }
}