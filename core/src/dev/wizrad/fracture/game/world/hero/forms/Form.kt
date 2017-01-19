package dev.wizrad.fracture.game.world.hero.forms

import com.badlogic.gdx.math.Vector2
import dev.wizrad.fracture.game.world.core.Behavior

interface Form {
  val type: Type
  val behavior: Behavior

  fun defineFixtures(size: Vector2)

  enum class Type {
    SingleJump,
    SpaceJump,
    Rebound,
    Spear
  }
}