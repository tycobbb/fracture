package dev.wizrad.fracture.game.world.support

import dev.wizrad.fracture.game.world.core.Entity

class EntitySequence {
  // MARK: Properties
  private val collections = mutableListOf<Collection<Entity>>()

  // MARK: Builder
  fun first(entity: Entity) = then(entity)
  fun then(entity: Entity): EntitySequence {
    collections.add(listOf(entity))
    return this
  }

  fun first(entities: Collection<Entity>) = then(entities)
  fun then(entities: Collection<Entity>): EntitySequence {
    collections.add(entities)
    return this
  }

  // MARK: Output
  fun toArray(): Array<Entity> {
    return collections.flatten().toTypedArray()
  }
}
