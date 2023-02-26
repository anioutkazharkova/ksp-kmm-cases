package com.azharkova.kmm.plugin.domain

import com.azharkova.kmm.plugin.util.isUseCase
import com.azharkova.kmm.plugin.util.usecaseName
import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.descriptors.*
import org.jetbrains.kotlin.descriptors.annotations.Annotations
import org.jetbrains.kotlin.descriptors.impl.SimpleFunctionDescriptorImpl
import org.jetbrains.kotlin.incremental.components.LookupLocation
import org.jetbrains.kotlin.ir.types.defaultType
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.resolve.BindingContext
import org.jetbrains.kotlin.resolve.constants.KClassValue
import org.jetbrains.kotlin.resolve.descriptorUtil.module
import org.jetbrains.kotlin.resolve.extensions.SyntheticResolveExtension
import org.jetbrains.kotlin.resolve.scopes.DescriptorKindFilter
import org.jetbrains.kotlin.resolve.scopes.findFirstFunction
import org.jetbrains.kotlin.resolve.scopes.getDescriptorsFiltered
import org.jetbrains.kotlin.types.*

class SyntethicExtension(): SyntheticResolveExtension {
    /**
     * Ensure companion is added to the class
     */
    override fun getSyntheticCompanionObjectNameIfNeeded(thisDescriptor: ClassDescriptor): Name? =
        Names.DEFAULT_COMPANION

    override fun getSyntheticFunctionNames(thisDescriptor: ClassDescriptor): List<Name> =
        if (thisDescriptor.isCompanionObject && thisDescriptor.isUsecaseCompanion) {
            listOf(Names.USECASE_METHOD)
        } else {
            emptyList()
        }

    override fun generateSyntheticMethods(
        thisDescriptor: ClassDescriptor,
        name: Name,
        bindingContext: BindingContext,
        fromSupertypes: List<SimpleFunctionDescriptor>,
        result: MutableCollection<SimpleFunctionDescriptor>
    ) {
        if (name != Names.USECASE_METHOD) return
        val classDescriptor = getForCompanion(thisDescriptor) ?: return
        //var apiClazz: ClassDescriptor? = null
        var params = mutableListOf<KotlinType>()
       /* thisDescriptor.annotations?.findAnnotation(usecaseName)?.allValueArguments?.forEach{
            (name, value) ->
            if (name.asString() == "repo") {
                val  apiRef = (value.value as KClassValue.Value.NormalClass).classId

                apiClazz =  thisDescriptor.module.findClassAcrossModuleDependencies(
                    apiRef
                )
            }
            if (name.asString() == "request") {
apiClazz?.getMemberScope(TypeSubstitution.EMPTY)?.getDescriptorsFiltered(DescriptorKindFilter.FUNCTIONS) {
    it == Name.identifier(value.toString())

}?.firstOrNull()?.let {
    val returnType = (it as? FunctionDescriptor)?.returnType
    val valueType = (it as? FunctionDescriptor)?.valueParameters?.firstOrNull()?.type

    params.addAll(listOfNotNull(returnType, valueType))
}
            }
        }*/
        result.add(createGetterDescriptor(thisDescriptor, classDescriptor, params))
    }

    private fun getForCompanion(descriptor: ClassDescriptor): ClassDescriptor? =
        if (descriptor.isUsecaseCompanion) {
            descriptor.containingDeclaration as ClassDescriptor
        } else {
            null
        }
}

val ClassDescriptor.isUsecaseCompanion
    get() = isCompanionObject && (containingDeclaration as ClassDescriptor).isUseCase()

fun createGetterDescriptor(
    companionClass: ClassDescriptor,
    clazz: ClassDescriptor,
    params: List<KotlinType>
): SimpleFunctionDescriptor {
    val function = SimpleFunctionDescriptorImpl.create(
        companionClass,
        Annotations.EMPTY,
        Names.USECASE_METHOD,
        CallableMemberDescriptor.Kind.SYNTHESIZED,
        companionClass.source
    )

    val usecaseClass = clazz.module.findClassAcrossModuleDependencies(
        ClassIds.COROUTINE_USE_CASE
    )!!

    /*val unit = clazz.module.findClassAcrossModuleDependencies(
        ClassIds.UNIT
    )!!
    var returnType = KotlinTypeFactory.simpleNotNullType(
        TypeAttributes.Empty,
        unit,
        emptyList()
    )*/

    val returnType = KotlinTypeFactory.simpleNotNullType(
        TypeAttributes.Empty,
       usecaseClass,
        params.map {
            TypeProjectionImpl(it)
        }
    )


    function.initialize(
        null,
        companionClass.thisAsReceiverParameter,
        emptyList(),
        emptyList(),
        returnType,
        Modality.FINAL,
        DescriptorVisibilities.PUBLIC
    )

    return function
}