package dev.wizrad.fracture.game.world.core

abstract class Behavior {
  open fun start() {
  }

  open fun update(delta: Float) {
  }

  open fun step(delta: Float) {
  }

  open fun destroy() {
  }
}
