package dev.wizrad.fracture.game.world.core

class EntitySequence {
  // MARK: Properties
  private val collections = mutableListOf<Collection<Entity>>()

  // MARK: Builder
  fun then(entity: Entity): EntitySequence {
    collections.add(listOf(entity))
    return this
  }

  fun then(entities: Collection<Entity>): EntitySequence {
    collections.add(entities)
    return this
  }

  // MARK: Output
  fun toArray(): Array<Entity> {
    return collections.flatten().toTypedArray()
  }
}
