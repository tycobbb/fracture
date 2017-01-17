package dev.wizrad.fracture.game.world

import dev.wizrad.fracture.game.world.core.Entity
import dev.wizrad.fracture.game.world.support.EntitySequence

class Level: Entity(parent = null) {
  // MARK: Entity
  override val name = "Level"

  // MARK: Children
  val hero = Hero()

  // MARK: Lifecycle
  override fun children(sequence: EntitySequence) =
    super.children(sequence)
      .then(hero)
}