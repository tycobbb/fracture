package dev.wizrad.fracture.game.world.core

abstract class State: Behavior() {
  // MARK: Properties
  protected var frames: Int = 0

  // MARK: Sequence
  abstract fun nextState(): State?

  // MARK: Lifecycle
  override fun update(delta: Float) {
    super.update(delta)
    frames++
  }
}
