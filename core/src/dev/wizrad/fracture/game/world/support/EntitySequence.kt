package dev.wizrad.fracture.game.world.support

import dev.wizrad.fracture.game.world.core.EntityBase

class EntitySequence {
  // MARK: Properties
  private val collections = mutableListOf<Collection<EntityBase>>()

  // MARK: Builder
  fun first(entity: EntityBase) = then(entity)
  fun then(entity: EntityBase): EntitySequence {
    collections.add(listOf(entity))
    return this
  }

  fun first(entities: Collection<EntityBase>) = then(entities)
  fun then(entities: Collection<EntityBase>): EntitySequence {
    collections.add(entities)
    return this
  }

  // MARK: Output
  fun toArray(): Array<EntityBase> {
    return collections.flatten().toTypedArray()
  }
}
