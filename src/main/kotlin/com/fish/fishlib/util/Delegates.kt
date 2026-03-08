package com.fish.fishlib.util

import com.fish.fishlib.util.extension.orElseGet
import com.fish.fishlib.util.extension.unit
import net.minecraft.core.component.DataComponentType
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.entity.BlockEntity
import net.minecraft.world.level.block.state.properties.Property
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

fun <T : Comparable<T>> BlockEntity.property(
    propertyBlock: Property<T>,
    updateServer: Boolean
) = object : ReadWriteProperty<Any?, T> {
    override fun getValue(thisRef: Any?, property: KProperty<*>) =
        this@property.blockState.getValue(propertyBlock)

    override fun setValue(thisRef: Any?, property: KProperty<*>, value: T) {
        val stateNew = this@property.blockState.setValue(propertyBlock, value)
        this@property.level?.setBlock(
            this@property.blockPos,
            stateNew,
            if (updateServer)
                Block.UPDATE_ALL
            else Block.UPDATE_CLIENTS
        )
    }
}

fun <T> ItemStack.component(type: DataComponentType<T>, valueDefault: T? = null) = object : ReadWriteProperty<Any?, T> {
    override fun getValue(
        thisRef: Any?, property: KProperty<*>
    ) = this@component.get(type).orElseGet {
        check(valueDefault != null) {
            "Cannot ignore default value of nullable component $type"
        }
        this@component.set(type, valueDefault)
        valueDefault
    }

    override fun setValue(
        thisRef: Any?, property: KProperty<*>, value: T
    ) = this@component.set(type, value).unit()
}