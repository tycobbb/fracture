package dev.wizrad.fracture.game.world.core

import dev.wizrad.fracture.support.extensions.append

class EntitySequence {
  // MARK: Properties
  private val sequence = mutableListOf<Entity>()

  // MARK: Builder
  fun then(entity: Entity?): EntitySequence {
    if (entity != null) {
      sequence.append(entity)
    }

    return this
  }

  fun then(entities: Collection<Entity>): EntitySequence {
    sequence.addAll(entities)
    return this
  }

  // MARK: Output
  fun toArray(): Array<Entity> {
    return sequence.toTypedArray()
  }
}
