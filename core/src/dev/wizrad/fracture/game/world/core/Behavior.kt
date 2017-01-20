package dev.wizrad.fracture.game.world.core

import com.badlogic.gdx.math.Vector2

abstract class Behavior {
  open fun start() {
  }

  open fun update(delta: Float) {
  }

  open fun step(delta: Float) {
  }

  open fun destroy() {
  }

  companion object {
    val scratch1 = Vector2()
    val scratch2 = Vector2()
  }
}
