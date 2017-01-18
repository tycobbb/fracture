package dev.wizrad.fracture.game.world

import dev.wizrad.fracture.game.world.core.Behavior

interface Form: Behavior {
  enum class Type {
    SingleJump
  }
}