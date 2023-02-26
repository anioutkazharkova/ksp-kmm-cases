package com.azharkova.kmm.plugin.domain

import com.azharkova.kmm.plugin.util.*
import com.azharkova.kmm.plugin.util.SYNTHETIC_OFFSET
import org.jetbrains.kotlin.backend.common.ClassLoweringPass
import org.jetbrains.kotlin.backend.common.extensions.FirIncompatiblePluginAPI
import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.backend.common.lower.DeclarationIrBuilder
import org.jetbrains.kotlin.backend.wasm.ir2wasm.allSuperInterfaces
import org.jetbrains.kotlin.backend.wasm.ir2wasm.getSuperClass
import org.jetbrains.kotlin.cli.common.messages.CompilerMessageSeverity
import org.jetbrains.kotlin.cli.common.messages.MessageCollector
import org.jetbrains.kotlin.descriptors.ClassKind
import org.jetbrains.kotlin.descriptors.DescriptorVisibilities
import org.jetbrains.kotlin.descriptors.Modality
import org.jetbrains.kotlin.ir.UNDEFINED_OFFSET
import org.jetbrains.kotlin.ir.backend.js.utils.asString
import org.jetbrains.kotlin.ir.builders.*
import org.jetbrains.kotlin.ir.builders.declarations.*
import org.jetbrains.kotlin.ir.declarations.*
import org.jetbrains.kotlin.ir.expressions.*
import org.jetbrains.kotlin.ir.expressions.impl.IrFunctionExpressionImpl
import org.jetbrains.kotlin.ir.expressions.impl.IrGetValueImpl
import org.jetbrains.kotlin.ir.symbols.IrClassSymbol
import org.jetbrains.kotlin.ir.types.*
import org.jetbrains.kotlin.ir.types.impl.IrSimpleTypeImpl
import org.jetbrains.kotlin.ir.util.*
import org.jetbrains.kotlin.ir.util.addChild
import org.jetbrains.kotlin.name.CallableId
import org.jetbrains.kotlin.name.ClassId
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.name.Name


class AddLoweringPass (val pluginContext: IrPluginContext, private val messageCollector: MessageCollector) : ClassLoweringPass {
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
    @OptIn(FirIncompatiblePluginAPI::class)
    override fun lower(irClass: IrClass) {
        if (!irClass.toIrBasedDescriptor().isUseCase()) {
            return
        }
        irClass.toIrBasedDescriptor().annotations.forEach {
            messageCollector.report(CompilerMessageSeverity.WARNING, it.type.toString())
            messageCollector.report(
                CompilerMessageSeverity.WARNING,
                it.fqName?.asString().orEmpty()
            )
            it.allValueArguments.forEach { (name, value) ->
                messageCollector.report(CompilerMessageSeverity.WARNING, name.asString())
                messageCollector.report(CompilerMessageSeverity.WARNING, value.toString())

                val cls = (value as? kotlin.reflect.KClass<*>)
                messageCollector.report(
                    CompilerMessageSeverity.WARNING,
                    "clss: ${cls?.qualifiedName}"
                )
                if (name.asString() == "repo") {
                    (value as? IrClassReference)?.let {
                        messageCollector.report(
                            CompilerMessageSeverity.WARNING,
                            "class: ${it.classType.toString()}"
                        )
                    }
                    messageCollector.report(
                        CompilerMessageSeverity.WARNING,
                        "class: ${value.boxedValue()}"
                    )
                }
            }
        }

       // if (irClass.functions.filter { it.name.asString() == "invoke" }.toList().isEmpty()) {
            val api = pluginContext.referenceClass(FqName("com.azharkova.kmmkspcases.TestApi"))
                ?.createType(false, emptyList())
            val apiPoint = pluginContext.referenceClass(FqName("com.azharkova.kmmkspcases.TestApi"))
            api?.let {
                messageCollector.report(
                    CompilerMessageSeverity.WARNING,
                    "api: ${it.classFqName?.asString()}"
                )
            }
       /* val con = irClass.addSimpleDelegatingConstructor(
            superConstructor = irClass.constructors.first(),
            irBuiltIns = irBuiltIns,
            isPrimary = false
        )
            val param = con.addValueParameter {
            name = Name.identifier("repo")
            type = api!!.type
            visibility = DescriptorVisibilities.PUBLIC
        }*/
            val f = api?.getClass()?.functions?.firstOrNull { it.name.asString() == "test" }

      /*  val lazyProperty = irClass.addProperty {
            name = Name.identifier("lazyRepo")
            this.isVar = false
            this.visibility = DescriptorVisibilities.PUBLIC
        }.apply {
            addGetter {
                this.returnType = pluginContext.referenceClass(FqName("com.azharkova.kmmkspcases.TestApiImpl"))!!.defaultType
              irClass.makeLazyField( pluginContext.referenceClass(FqName("com.azharkova.kmmkspcases.TestApiImpl"))!!.defaultType)
            }
        }*/

            val property = irClass.addProperty {
                name = Name.identifier("repo")
                this.isVar = true
                visibility = DescriptorVisibilities.PUBLIC
            }.apply {
                backingField = irFactory.buildField {
                    name = Name.identifier("repo")
                    type = api!!.type
                    visibility = DescriptorVisibilities.PROTECTED
                }.also { field ->
                    field.parent = irClass
                }
                addGetter {
                    this.returnType = backingField!!.type
                    visibility = DescriptorVisibilities.PRIVATE
                    origin = IrDeclarationOrigin.DEFAULT_PROPERTY_ACCESSOR
                }
            }
            val propertyClassSymbol: IrClassSymbol = apiPoint!!
            val propertyClass: IrClass = propertyClassSymbol.owner
            val propertyFunction = f
            f?.let { function ->
                val newFunction = irClass.addFunction {
                    name = Name.identifier("invoke")
                    visibility = DescriptorVisibilities.PUBLIC
                    modality = Modality.OPEN
                    isSuspend = true
                    this.returnType = function.returnType
                }.apply {
                    dispatchReceiverParameter = irClass.thisReceiver?.copyTo(this)
                    val newFunction = this
                    newFunction.body =
                        DeclarationIrBuilder(pluginContext, newFunction.symbol).irBlockBody {
                            +irReturn(irCall(function).apply {
                                dispatchReceiver = irCall(property.getter!!).apply {
                                    dispatchReceiver =
                                        irGet(newFunction.dispatchReceiverParameter!!)
                                }
                            })
                        }
                }

            }

            api?.getClass()?.functions?.firstOrNull { it.name.asString() == "test" }?.let {
                messageCollector.report(CompilerMessageSeverity.WARNING, "func: ${it.name}")
            }


            irClass.apply {
                messageCollector.report(CompilerMessageSeverity.WARNING, this.name.asString())
                this.allSuperInterfaces().forEach {
                    messageCollector.report(CompilerMessageSeverity.WARNING, it.name.asString())
                }
                messageCollector.report(CompilerMessageSeverity.WARNING, "add class")
                val codingClass = this.addCodingClass(this.name.asString(), superTypes = listOf(pluginContext.typeOf(
                   pluginContext.referenceClass(FqName("kotlin.Unit"))!!,
                    pluginContext.referenceClass(f?.returnType?.getClass()?.classId!!)!!
                )))
                    .apply {
                    val con = this.addSimpleDelegatingConstructor(
                        superConstructor = irClass.getSuperClass(irBuiltIns)!!.constructors.first(),
                        irBuiltIns = irBuiltIns,
                        isPrimary = true
                    )
                    val param = con.addValueParameter {
                        name = Name.identifier("repo")
                        type = api!!.type
                        visibility = DescriptorVisibilities.PUBLIC
                    }
                }
                messageCollector.report(CompilerMessageSeverity.WARNING, "add companion")
                //this.addCodingCompanionObject()
                messageCollector.report(CompilerMessageSeverity.WARNING, irClass.dump())
                // this.generateCodingBody(codingClass)
            }
        //}

    }

    fun IrPluginContext.typeOf(first: IrClassSymbol, second: IrClassSymbol): IrType {
        val typeSymbol = referenceClass(ClassId(FqName("com.azharkova.core"), Name.identifier("CoroutineUseCase")))
        return typeSymbol!!.createType(
            hasQuestionMark = false,
            arguments = listOf(
                IrSimpleTypeImpl(
                    classifier = first,
                    hasQuestionMark = false,
                    arguments = emptyList(),
                    annotations = emptyList()
                ), IrSimpleTypeImpl(
                    classifier = second,
                    hasQuestionMark = false,
                    arguments = emptyList(),
                    annotations = emptyList()
                )
            )
        )
    }

    /*override fun lower(irClass: IrClass) {
        val propertyToClose: IrProperty = /* ... */
        val propertyClassSymbol: IrClassSymbol = pluginContext.findClassOf(propertyToClose)
        val propertyClass: IrClass = propertyClassSymbol.owner
        val propertyCloseFunction: IrSimpleFunction =
            propertyClass.functions.single { it.name == closeName }

        thisCloseFunction.body = DeclarationIrBuilder(pluginContext, thisCloseFunction.symbol).irBlockBody {
            +irCall(propertyCloseFunction).apply {
                dispatchReceiver = irCall(propertyToClose.getter!!).apply {
                    dispatchReceiver = irGet(thisCloseFunction.dispatchReceiverParameter!!)
                }
            }
        }
    }*/

    private fun IrClass.addDataProperty(dataField: IrField): IrProperty =
        addProperty {
            name = Name.identifier("repo")
            visibility = DescriptorVisibilities.PRIVATE
            isVar = true
        }.apply {
            backingField = dataField
        }

    private fun IrClass.addDataField(type: IrType, constructorParameter: IrValueParameter): IrField =
        irFactory
            .buildField {
                name = Name.identifier("repo")
                visibility = DescriptorVisibilities.PRIVATE
                this.type = type
                origin = IrDeclarationOrigin.PROPERTY_BACKING_FIELD
            }
            .apply {
                parent = this@addDataField

                initializer =
                    irFactory.createExpressionBody(
                        startOffset = UNDEFINED_OFFSET,
                        endOffset = UNDEFINED_OFFSET,
                        expression = IrGetValueImpl(
                            startOffset = UNDEFINED_OFFSET,
                            endOffset = UNDEFINED_OFFSET,
                            symbol = constructorParameter.symbol,
                            origin = IrStatementOrigin.INITIALIZE_PROPERTY_FROM_PARAMETER
                        )
                    )
            }

    private fun IrProperty.addDataGetter(returnType: IrType, mainClass: IrClass, dataField: IrField): IrSimpleFunction =
        addGetter {
            this.returnType = returnType
            visibility = DescriptorVisibilities.PRIVATE
            origin = IrDeclarationOrigin.DEFAULT_PROPERTY_ACCESSOR
        }.apply {
            val receiverParameter = mainClass.thisReceiver!!.copyTo(this)
            dispatchReceiverParameter = receiverParameter

            setBody(pluginContext) {
                +irReturn(
                    irGetField(
                        irGet(receiverParameter.type, receiverParameter.symbol),
                        dataField
                    )
                )
            }
        }

    private fun IrFunction.addBody(callableFunction: IrFunction, receiver: IrExpression) : IrBlockBody = DeclarationIrBuilder(pluginContext, this.symbol).irBlockBody {
val block = irReturn(irCall(
callableFunction.symbol, callableFunction.returnType).also{
it.dispatchReceiver =  receiver//irGetObjectValue(receiver.createType(false, emptyList()), receiver)
})
        +block
        +irCall(funPrintln).also {
            var concat = irConcat()
            concat.addArgument(irString("Block:\n\n" + "\n\n"))
            it.putValueArgument(0, concat)
        }
    }

   /* private fun IrClass.addRepoFunction(callableFunction: IrFunction): IrFunction =
        irFactory.buildFun {
            name = Name.identifier("invoke")
            visibility = DescriptorVisibilities.PUBLIC
            this.returnType = callableFunction.returnType
this.startOffset = UNDEFINED_OFFSET
            this.endOffset = UNDEFINED_OFFSET
            this.origin = IrDeclarationOrigin.DEFINED
            this.modality = Modality.FINAL
            //origin = IrDeclarationOrigin.
        }.apply {
            val thisReceiver = this@addFunction.thisReceiver!!
            dispatchReceiverParameter = thisReceiver.copyTo(this, type = thisReceiver.type)
        }*/


  /*  private fun makeExecute(
        function: IrFunction
    ): IrBlockBody {
        return DeclarationIrBuilder(pluginContext, function.symbol).irBlockBody {
            val block = irReturn()
            +block
            +irCall().also {

            }
        }
    }*/


    @OptIn(FirIncompatiblePluginAPI::class)
    private fun IrClass.addCodingClass(className: String, superTypes: List<IrType> = emptyList()): IrClass =
        irFactory
            .buildClass {
                name = Name.identifier("${className}Impl")
                kind = ClassKind.CLASS
                visibility = DescriptorVisibilities.PRIVATE
                modality = Modality.FINAL
                startOffset = SYNTHETIC_OFFSET
                endOffset = SYNTHETIC_OFFSET
            }
            .also(::addChild)
            .apply {
                this.superTypes = superTypes
               //superTypes = listOf(pluginContext.referenceClass(coroutineUsecase())!!.createType(false, emptyList()))
                //annotations = listOf(getExportObjCClassAnnotationCall(name = getFullCapitalizedName()))
               createImplicitParameterDeclarationWithWrappedDescriptor()
            }

    private fun IrClass.addCodingCompanionObject(): IrClass =
        irFactory
            .buildClass {
                name = Name.identifier("Companion")
                kind = ClassKind.OBJECT
                visibility = DescriptorVisibilities.PUBLIC
                modality = Modality.FINAL
                isCompanion = true
            }
            .also(::addChild)
            .apply {
                //superTypes = listOf(symbols.nsObjectType, symbols.nsSecureCodingMetaType)
                createImplicitParameterDeclarationWithWrappedDescriptor()
            }



    private fun IrClass.makeLazyField(
       returnType: IrType
    ): IrField {
        val lazyFunction = lazyFunction
        check(lazyFunction != null) { "kotlin.Lazy not found" }
        val lazyType = lazyFunction.returnType.getClass()!!.typeWith(returnType)
        val field = this.addField {
            type = lazyType
            name = Name.identifier("${returnType.asString()}")
            visibility = DescriptorVisibilities.PRIVATE
        }
        field.initializer = with(DeclarationIrBuilder(pluginContext, field.symbol)) {
            val factoryFunction = field.factory.buildFun {
                name = Name.special("<internal_injection_initializer>")
                this.returnType = returnType
                visibility = DescriptorVisibilities.LOCAL
                origin = IrDeclarationOrigin.LOCAL_FUNCTION_FOR_LAMBDA
            }.apply {
                parent = field
                body = DeclarationIrBuilder(pluginContext, symbol).irBlockBody {
                    +irReturn(
                        irCallConstructor(
                            returnType.getClass()!!.primaryConstructor!!.symbol,
                            emptyList()
                        )
                    )
                }
            }
            val functionExpression = IrFunctionExpressionImpl(
                startOffset,
                endOffset,
                pluginContext.irBuiltIns.functionN(0).typeWith(returnType),
                factoryFunction,
                IrStatementOrigin.LAMBDA
            )
            irExprBody(
                irCall(lazyFunction.symbol, lazyType).also {
                    it.putTypeArgument(0, returnType)
                    it.putValueArgument(0, functionExpression)
                }
            )
        }
        return field
    }
}