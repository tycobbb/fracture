package dev.wizrad.fracture.game.world.core

import dev.wizrad.fracture.game.core.Updatable

abstract class Behavior: Updatable {
  override fun update(delta: Float) {
  }

  open fun step(delta: Float) {
  }

  open fun destroy() {
  }
}
