package com.azharkova.kmm.plugin.domain

import com.azharkova.kmm.plugin.util.*
import com.azharkova.kmm.plugin.util.addChild
import org.jetbrains.kotlin.backend.common.ClassLoweringPass
import org.jetbrains.kotlin.backend.common.extensions.FirIncompatiblePluginAPI
import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.backend.common.lower.DeclarationIrBuilder
import org.jetbrains.kotlin.backend.jvm.ir.erasedUpperBound
import org.jetbrains.kotlin.backend.wasm.ir2wasm.getSuperClass
import org.jetbrains.kotlin.cli.common.messages.CompilerMessageSeverity
import org.jetbrains.kotlin.cli.common.messages.MessageCollector
import org.jetbrains.kotlin.descriptors.*
import org.jetbrains.kotlin.resolve.constants.KClassValue.Value.NormalClass
import org.jetbrains.kotlin.fir.resolve.defaultType
import org.jetbrains.kotlin.ir.ObsoleteDescriptorBasedAPI
import org.jetbrains.kotlin.ir.builders.*
import org.jetbrains.kotlin.ir.builders.declarations.*
import org.jetbrains.kotlin.ir.declarations.*
import org.jetbrains.kotlin.ir.declarations.impl.IrFunctionImpl
import org.jetbrains.kotlin.ir.expressions.IrBlockBody
import org.jetbrains.kotlin.ir.expressions.IrBody
import org.jetbrains.kotlin.ir.expressions.IrStatementOrigin
import org.jetbrains.kotlin.ir.expressions.addArgument
import org.jetbrains.kotlin.ir.expressions.impl.IrDelegatingConstructorCallImpl
import org.jetbrains.kotlin.ir.expressions.impl.IrFunctionExpressionImpl
import org.jetbrains.kotlin.ir.expressions.impl.IrGetValueImpl
import org.jetbrains.kotlin.ir.expressions.impl.IrInstanceInitializerCallImpl
import org.jetbrains.kotlin.ir.interpreter.toIrConst
import org.jetbrains.kotlin.ir.symbols.IrClassSymbol
import org.jetbrains.kotlin.ir.symbols.IrClassifierSymbol
import org.jetbrains.kotlin.ir.symbols.IrSimpleFunctionSymbol
import org.jetbrains.kotlin.ir.symbols.IrSymbol
import org.jetbrains.kotlin.ir.symbols.impl.IrSimpleFunctionSymbolImpl
import org.jetbrains.kotlin.ir.types.*
import org.jetbrains.kotlin.ir.types.impl.IrSimpleTypeImpl
import org.jetbrains.kotlin.ir.types.impl.buildSimpleType
import org.jetbrains.kotlin.ir.util.*
import org.jetbrains.kotlin.js.descriptorUtils.getJetTypeFqName
import org.jetbrains.kotlin.name.CallableId
import org.jetbrains.kotlin.name.ClassId
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.psi2ir.findSingleFunction
import org.jetbrains.kotlin.resolve.descriptorUtil.getSuperClassNotAny
import org.jetbrains.kotlin.resolve.descriptorUtil.module
import org.jetbrains.kotlin.types.TypeSubstitution

class UseCaseLoweringPass(private val pluginContext: IrPluginContext, private val messageCollector: MessageCollector) :
    ClassLoweringPass {
    private val irFactory = pluginContext.irFactory
    private val irBuiltIns = pluginContext.irBuiltIns

    private val lazyFunction by lazy {
        pluginContext.referenceFunctions(CallableId(FqName("kotlin"), Name.identifier("lazy"))).firstOrNull()?.owner
    }

    @OptIn(FirIncompatiblePluginAPI::class)
    val funPrintln = pluginContext.referenceFunctions(FqName("kotlin.io.println"))
        .single {
            val parameters = it.owner.valueParameters
            parameters.size == 1 && parameters[0].type == pluginContext.irBuiltIns.anyNType
        }

    @OptIn(FirIncompatiblePluginAPI::class, ObsoleteDescriptorBasedAPI::class)
    override fun lower(irClass: IrClass) {
        if (!irClass.toIrBasedDescriptor().isUseCase()) {
            return
        }


        var apiRef: IrType? = null
        var request: String = ""
        var paramIn: IrValueParameter? = null
        irClass.toIrBasedDescriptor().annotations.firstOrNull { it.fqName == usecaseName }?.let {
            it.allValueArguments.forEach { (name, value) ->

                if (name.asString() == "repo") {
                    apiRef = pluginContext.referenceClass((value.value as NormalClass).classId)?. defaultType


                }
                if (name.asString() == "request") {

                    request = value.toString().replace("\"","")
                    messageCollector.report(CompilerMessageSeverity.WARNING, request)
                }
            }
        }
        val apiImpl = pluginContext.referenceClass(FqName(apiRef?.classFqName?.asString().orEmpty() + "Impl"))!!.defaultType

        val requestFunction = apiImpl?.getClass()?.functions?.firstOrNull { it.name.asString() == request }
        messageCollector.report(CompilerMessageSeverity.WARNING, requestFunction?.name?.asString().orEmpty())

       paramIn = requestFunction?.valueParameters?.firstOrNull()

        val implClass = irClass.addUseCaseImpl(paramOut = requestFunction?.returnType)

       val repoFunction = implClass.addFunction {
           name = Name.identifier("repo")
           visibility = DescriptorVisibilities.PRIVATE
           returnType = apiImpl//pluginContext.referenceClass(FqName(apiRef?.classFqName?.asString().orEmpty() + "Impl"))!!.defaultType
        }.apply {
           val function = this
           dispatchReceiverParameter = implClass.thisReceiver?.copyTo(this)
           val implName = this.returnType.classFqName?.asString().orEmpty()//.classFqName?.asString() + "Impl"
           this.body = createLazyBody(function)
       }

        val propertyClassSymbol: IrClassSymbol = apiImpl?.getClass()?.symbol!!
        val propertyClass: IrClass = propertyClassSymbol.owner
        val propertyFunction = requestFunction

        val executeFunction = pluginContext.referenceClass(ClassIds.GENERIC_USE_CASE)?.owner?.functions?.filter {
            it.name.asString() == "execute"
        }?.firstOrNull()

        val invokeFunction = implClass?.superTypes?.lastOrNull()?.getClass()?.functions?.filter {
            it.name.asString() == "request"
        }?.firstOrNull()


        requestFunction?.let { function ->
            val newFunction = implClass.addFunction {
                name = Name.identifier("execute")
                visibility = DescriptorVisibilities.PUBLIC
                modality = Modality.OPEN
              //  isFakeOverride = true
                isSuspend = true
                this.returnType = function.returnType
               // origin = IrDeclarationOrigin.FAKE_OVERRIDE
            }.apply {
               overriddenSymbols = listOf(executeFunction?.symbol!!)
                addValueParameter {
                    name = Name.identifier("param")
                    type = paramIn?.type ?: pluginContext.referenceClass(Names.UNIT)!!.defaultType
                }
                dispatchReceiverParameter = implClass.thisReceiver?.copyTo(this)
                val newFunction = this

                newFunction.body =
                    DeclarationIrBuilder(pluginContext, newFunction.symbol).irBlockBody {
                        val block = irReturn(irCall(requestFunction!!.symbol, requestFunction!!.returnType).apply {
                            paramIn?.let {
                                this.putValueArgument(0, irGet(paramIn))
                            }
                            dispatchReceiver =
                                irCall(repoFunction.symbol, repoFunction.returnType).apply {
                                    paramIn?.let {
                                        this.putValueArgument(0, irGet(paramIn))
                                    }
                                    dispatchReceiver =
                                        irGet(newFunction.dispatchReceiverParameter!!)
                                }
                        })
                       +block

                    }
            }
        }

        generateFactory(irClass.companionObject() as IrClass, implClass)
        messageCollector.report(CompilerMessageSeverity.WARNING, irClass.dump())
    }

    private fun createLazyBody(
        function: IrFunction
    ): IrBlockBody {

        val field = makeLazyField(function, function.parentClassOrNull!!)
        val getValueFunction =
            field.type.getClass()!!.properties.first { it.name.identifier == "value" }.getter!!
        return DeclarationIrBuilder(pluginContext, function.symbol).irBlockBody {
            +irReturn(
                irCall(getValueFunction.symbol, function.returnType).also {
                    it.dispatchReceiver = irGetField(
                        IrGetValueImpl(
                            startOffset,
                            endOffset,
                            (function.dispatchReceiverParameter
                                ?: function.parentClassOrNull!!.thisReceiver)!!.symbol
                        ), field
                    )
                }
            )
        }
    }


    private fun generateFactory(companionClass: IrClass, implClass: IrClass) {
        var function = companionClass.functions.firstOrNull() { it.name == Names.USECASE_METHOD }
        function?.let {
            function?.dispatchReceiverParameter = companionClass.thisReceiver?.copyTo(function)
            function?.body = pluginContext.blockBody(function.symbol) {
                +irReturn(
                    irGetObject(implClass.symbol)
                )
            }
        }
    }

    private fun IrClass.addUseCaseImpl(paramIn: IrType? = null, paramOut: IrType? = null):IrClass {
        val useCaseCls = irFactory.buildClass{
            name = Names.USECASE_IMPL
            modality = Modality.FINAL
            visibility = DescriptorVisibilities.PUBLIC
            kind = ClassKind.OBJECT
        }

       // useCaseCls.superTypes = listOf(pluginContext.irType(ClassIds.COROUTINE_USE_CASE, false, emptyList()))
        pluginContext.typeWith(paramIn, paramOut)?.let {
            useCaseCls.superTypes = listOf(it)
        }

        this.addChild(useCaseCls)

        val receiver = buildValueParameter(useCaseCls) {
            name = Name.special("<this>")
            type = IrSimpleTypeImpl(
                classifier = useCaseCls.symbol,
                hasQuestionMark = false,
                arguments = emptyList(),
                annotations = emptyList()
            )
            origin = IrDeclarationOrigin.INSTANCE_RECEIVER
        }
        useCaseCls.thisReceiver = receiver
        receiver.parent = useCaseCls

        val constructor = useCaseCls.addConstructor {
            visibility = DescriptorVisibilities.PRIVATE
            returnType = useCaseCls.defaultType
            isPrimary = true
        }

        constructor.body = pluginContext.blockBody(constructor.symbol) {
            val any = irBuiltIns.anyClass
            val anyType = pluginContext.irBuiltIns.anyType
            + IrDelegatingConstructorCallImpl(
                startOffset,
                endOffset,
                anyType,
               any.constructors.single(),
                0,
                0
            )

            + IrInstanceInitializerCallImpl(
                startOffset,
                endOffset,
                useCaseCls.symbol,
                useCaseCls.defaultType
            )
        }

        return useCaseCls
    }


    @OptIn(FirIncompatiblePluginAPI::class)
    private fun makeLazyField(
        function: IrFunction,
        clazz: IrClass
    ): IrField {
        val lazyFunction = lazyFunction
        check(lazyFunction != null) { "kotlin.Lazy not found" }
        val lazyType = lazyFunction.returnType.getClass()!!.typeWith(function.returnType)
        val field = clazz.addField {
            type = lazyType
            name = Name.identifier("_repo__${function.name.asString()}")
            visibility = DescriptorVisibilities.PRIVATE
            startOffset = function.startOffset
            endOffset = function.endOffset
        }
        field.initializer = with(DeclarationIrBuilder(pluginContext, field.symbol)) {
            val factoryFunction = field.factory.buildFun {
                name = Name.special("<internal_injection_initializer>")
                returnType = function.returnType
                visibility = DescriptorVisibilities.LOCAL
                origin = IrDeclarationOrigin.LOCAL_FUNCTION_FOR_LAMBDA
            }.apply {
                parent = field
                val implName = function.returnType.classFqName?.asString().orEmpty()// + "Impl"
                body = DeclarationIrBuilder(pluginContext, symbol).irBlockBody {
                    +irReturn(
                        irCallConstructor(
                            pluginContext.referenceClass(FqName(implName))!!.constructors.first(),
                            emptyList()
                        )
                    )
                }
            }
            val functionExpression = IrFunctionExpressionImpl(
                startOffset,
                endOffset,
                pluginContext.irBuiltIns.functionN(0).typeWith(function.returnType),
                factoryFunction,
                IrStatementOrigin.LAMBDA
            )
            irExprBody(
                irCall(lazyFunction.symbol, lazyType).also {
                    it.putTypeArgument(0, function.returnType)
                    it.putValueArgument(0, functionExpression)
                }
            )
        }
        return field
    }

}

fun IrPluginContext.blockBody(
    symbol: IrSymbol,
    block: IrBlockBodyBuilder.() -> Unit
): IrBlockBody =
    DeclarationIrBuilder(this, symbol).irBlockBody { block() }


@OptIn(FirIncompatiblePluginAPI::class)
fun IrPluginContext.typeWith(paramIn: IrType? = null, paramOut: IrType? = null) : IrType? {
    val unitType = referenceClass(Names.ANY)!!.defaultType
    return this.irType(ClassIds.GENERIC_USE_CASE, false, emptyList()).classifierOrFail.typeWith(
        paramIn ?: unitType, paramOut ?: unitType
    )
}
val IrType.classifierOrFail: IrClassifierSymbol
    get() = classifierOrNull ?: error("Can't get classifier of ${render()}")

val IrType.classifierOrNull: IrClassifierSymbol?
    get() = when (this) {
        is IrSimpleType -> classifier
        else -> null
    }

internal fun IrPluginContext.irType(
    classId: ClassId,
    nullable: Boolean = false,
    arguments: List<IrTypeArgument> = emptyList()
): IrType = referenceClass(classId)!!.createType(hasQuestionMark = nullable, arguments = arguments)

