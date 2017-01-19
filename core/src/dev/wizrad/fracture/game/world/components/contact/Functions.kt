package dev.wizrad.fracture.game.world.components.contact

// MARK: Operations
infix fun Short.and(type: ContactType): Int {
  return toInt().and(type.value)
}
