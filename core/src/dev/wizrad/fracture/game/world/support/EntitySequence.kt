package dev.wizrad.fracture.game.world.support

import dev.wizrad.fracture.game.world.core.BaseEntity

class EntitySequence {
  // MARK: Properties
  private val collections = mutableListOf<Collection<BaseEntity>>()

  // MARK: Builder
  fun first(entity: BaseEntity) = then(entity)
  fun then(entity: BaseEntity): EntitySequence {
    collections.add(listOf(entity))
    return this
  }

  fun first(entities: Collection<BaseEntity>) = then(entities)
  fun then(entities: Collection<BaseEntity>): EntitySequence {
    collections.add(entities)
    return this
  }

  // MARK: Output
  fun toArray(): Array<BaseEntity> {
    return collections.flatten().toTypedArray()
  }
}
