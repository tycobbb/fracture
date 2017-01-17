package dev.wizrad.fracture.game.world

import com.badlogic.gdx.math.Vector2
import dev.wizrad.fracture.game.world.core.Entity
import dev.wizrad.fracture.game.world.support.EntitySequence

class Level: Entity(parent = null) {
  // MARK: Entity
  override val name = "Level"

  // MARK: Children
  val hero = Hero(parent = this)

  // MARK: Properties
  val size = Vector2(320.0f, 568.0f)

  // MARK: Lifecycle
  override fun children(sequence: EntitySequence) =
    super.children(sequence)
      .then(hero)
}