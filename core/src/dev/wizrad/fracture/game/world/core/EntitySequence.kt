package dev.wizrad.fracture.game.world.core

class EntitySequence {
  // MARK: Properties
  private val collections = mutableListOf<Collection<EntityBase>>()

  // MARK: Builder
  fun then(entity: EntityBase): EntitySequence {
    collections.add(listOf(entity))
    return this
  }

  fun then(entities: Collection<EntityBase>): EntitySequence {
    collections.add(entities)
    return this
  }

  // MARK: Output
  fun toArray(): Array<EntityBase> {
    return collections.flatten().toTypedArray()
  }
}
