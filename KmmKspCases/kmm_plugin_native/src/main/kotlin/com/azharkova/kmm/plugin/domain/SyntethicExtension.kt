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

    override fun getSyntheticCompanionObjectNameIfNeeded(thisDescriptor: ClassDescriptor): Name? =
        if (thisDescriptor.isUsecase) {
            Names.DEFAULT_COMPANION
        } else {
            null
        }

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

        var params = mutableListOf<KotlinType>()

        val any = thisDescriptor.module.findClassAcrossModuleDependencies(
            ClassIds.ANY
        )?.defaultType
        params.addAll(listOf(any!!,any!!))
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

val ClassDescriptor.isUsecase
    get() = this.isUseCase()

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


    val returnType = KotlinTypeFactory.simpleNotNullType(
        TypeAttributes.Empty,
       usecaseClass,
        emptyList()
      /*  params.map {
            TypeProjectionImpl(it)
        }*/
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