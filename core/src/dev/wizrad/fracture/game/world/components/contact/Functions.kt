package dev.wizrad.fracture.game.world.components.contact

import com.badlogic.gdx.physics.box2d.Filter

// MARK: Filter
fun Filter.set(type: ContactType) {
  categoryBits = type.category
  maskBits = type.mask
}

// MARK: Operators
operator fun Short.not() = toInt().inv().toShort()
operator fun ContactType.not() = value.inv().toShort()

infix fun Short.or(other: Short) = toInt().or(other.toInt()).toShort()
infix fun Short.and(other: Short) = toInt().and(other.toInt()).toShort()

infix fun Short.or(type: ContactType) = toInt().or(type.value).toShort()
infix fun Short.and(type: ContactType) = toInt().and(type.value)

infix fun ContactType.or(type: ContactType) = value.or(type.value).toShort()
infix fun ContactType.and(type: ContactType): Short = value.and(type.value).toShort()
