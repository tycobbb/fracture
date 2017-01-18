package dev.wizrad.fracture.game.world.core

import dev.wizrad.fracture.support.debugPrefix

abstract class State: Behavior() {
  // MARK: Properties
  protected var frame: Int = 0

  // MARK: Sequence
  abstract fun nextState(): State?

  // MARK: Lifecycle
  override fun update(delta: Float) {
    super.update(delta)
    frame++
  }

  // MARK: Debugging
  override fun toString(): String {
    return "[$debugPrefix frame=$frame]"
  }
}
