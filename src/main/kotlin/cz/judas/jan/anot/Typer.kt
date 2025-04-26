package cz.judas.jan.anot

import cz.judas.jan.anot.ast.FunctionParameter
import cz.judas.jan.anot.ast.FunctionSignature
import cz.judas.jan.anot.ast.PartiallyTypedFunction
import cz.judas.jan.anot.ast.PartiallyTypedSourceFile
import cz.judas.jan.anot.ast.typed.FullyQualifiedName
import cz.judas.jan.anot.ast.typed.Function
import cz.judas.jan.anot.ast.untyped.TopLevelDefinition
import cz.judas.jan.anot.ast.typed.Annotation as TypedAnnotation
import cz.judas.jan.anot.ast.typed.Expression as TypedExpression
import cz.judas.jan.anot.ast.typed.Package as TypedSourceFile
import cz.judas.jan.anot.ast.untyped.Annotation as UntypedAnnotation
import cz.judas.jan.anot.ast.untyped.Expression as UntypedExpression
import cz.judas.jan.anot.ast.untyped.SourceFile as UntypedSourceFile

class Typer {
    fun addSignatureTypeInfo(filePackage: FullyQualifiedName, untypedSourceFile: UntypedSourceFile): PartiallyTypedSourceFile {
        val importedSymbols = untypedSourceFile.imports
            .map { if (it.isAbsolute) { FullyQualifiedName(it.importedPath) } else { filePackage.child(it.importedPath) } }
            .associateBy { it.name() }
        val localDefinitions = untypedSourceFile.definitions
            .associate { it.name to filePackage.child(it.name) }
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
            .map { resolveFunction(it, Scope.topLevel(sourceFile.imports, symbolMap), symbolMap) }

        return TypedSourceFile(sourceFile.filePackage, functions)
    }

    private fun resolveSignature(untypedFunction: TopLevelDefinition.Function, importedSymbols: Map<String, FullyQualifiedName>): FunctionSignature {
        return FunctionSignature(
            untypedFunction.name,
            untypedFunction.parameters.map { FunctionParameter(it.name, importedSymbols.getValue(it.type)) },
            untypedFunction.returnType?.let { importedSymbols.getValue(it) } ?: Stdlib.void,
        )
    }

    private fun resolveFunction(
        partiallyTypedFunction: PartiallyTypedFunction,
        scope: Scope,
        symbolMap: SymbolMap,
    ): Function {
        val innerScope = scope.child(partiallyTypedFunction.signature.parameters.associate { it.name to Scope.Entry.Variable(it.type) })
        return Function(
            partiallyTypedFunction.annotations.map { resolveAnnotation(it, innerScope) },
            partiallyTypedFunction.signature.name,
            partiallyTypedFunction.signature.parameters,
            partiallyTypedFunction.signature.returnType,
            partiallyTypedFunction.body.map { resolveExpression(it, innerScope, symbolMap) }
        )
    }

    private fun resolveAnnotation(untypedAnnotation: UntypedAnnotation, scope: Scope): TypedAnnotation {
        return TypedAnnotation(scope.getClass(untypedAnnotation.type).type)
    }

    private fun resolveFunctionCall(
        untypedFunctionCall: UntypedExpression.FunctionCall,
        scope: Scope,
        symbolMap: SymbolMap,
    ): TypedExpression.FunctionCall {
        val function = scope.getFunction(untypedFunctionCall.functionName)
        return TypedExpression.FunctionCall(
            function.type,
            untypedFunctionCall.arguments.map { resolveExpression(it, scope, symbolMap) },
            function.signature.returnType,
        )
    }

    private fun resolveMethodCall(
        untypedMethodCall: UntypedExpression.MethodCall,
        scope: Scope,
        symbolMap: SymbolMap,
    ): TypedExpression.MethodCall {
        val receiver = resolveVariable(untypedMethodCall.receiver, scope)
        return TypedExpression.MethodCall(
            receiver,
            untypedMethodCall.methodName,
            untypedMethodCall.arguments.map { resolveExpression(it, scope, symbolMap) },
            symbolMap.classes.getValue(receiver.type).methods.getValue(untypedMethodCall.methodName).returnType,
        )
    }

    private fun resolveVariable(
        name: String,
        scope: Scope,
    ): TypedExpression.VariableReference {
        return TypedExpression.VariableReference(name, scope.getVariable(name).type)
    }

    private fun resolveExpression(untypedExpression: UntypedExpression, scope: Scope, symbolMap: SymbolMap): TypedExpression {
        return when (untypedExpression) {
            is UntypedExpression.StringConstant -> TypedExpression.StringConstant(untypedExpression.value)
            is UntypedExpression.FunctionCall -> resolveFunctionCall(untypedExpression, scope, symbolMap)
            is UntypedExpression.MethodCall -> resolveMethodCall(untypedExpression, scope, symbolMap)
            is UntypedExpression.VariableReference -> resolveVariable(untypedExpression.name, scope)
        }
    }

    private class Scope(
        private val symbols: Map<String, Entry>,
        private val parent: Scope?
    ) {
        sealed interface Entry {
            data class Function(val type: FullyQualifiedName, val signature: FunctionSignature): Entry
            data class Class(val type: FullyQualifiedName): Entry
            data class Variable(val type: FullyQualifiedName): Entry
        }

        fun getClass(name: String): Entry.Class {
            return get(name) as Entry.Class
        }

        fun getFunction(name: String): Entry.Function {
            return get(name) as Entry.Function
        }

        fun getVariable(name: String): Entry.Variable {
            return get(name) as Entry.Variable
        }

        fun child(newSymbols: Map<String, Entry>): Scope {
            return Scope(newSymbols, this)
        }

        private fun get(name: String): Entry {
            return symbols[name] ?: if (parent !== null) parent.get(name) else { throw RuntimeException("Symbol ${name} not found") }
        }

        companion object {
            fun topLevel(imports: Map<String, FullyQualifiedName>, symbolMap: SymbolMap): Scope {
                return Scope(
                    imports.mapValues { (_, type) -> resolve(type, symbolMap) },
                    null,
                )
            }

            private fun resolve(type: FullyQualifiedName, symbolMap: SymbolMap): Entry {
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