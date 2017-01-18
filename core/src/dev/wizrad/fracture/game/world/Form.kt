package dev.wizrad.fracture.game.world

import dev.wizrad.fracture.game.world.core.Behavior

interface Form {
  val type: Type
  val behavior: Behavior

  enum class Type {
    SingleJump,
    DoubleJump
  }
}