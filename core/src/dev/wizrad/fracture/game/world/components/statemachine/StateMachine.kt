package dev.wizrad.fracture.game.world.components.statemachine

import dev.wizrad.fracture.game.world.core.Behavior
import dev.wizrad.fracture.support.Tag
import dev.wizrad.fracture.support.className
import dev.wizrad.fracture.support.debug

open class StateMachine(
  initialState: State): Behavior() {

  // MARK: Properties
  var state: State = initialState

  // MARK: Lifecycle
  override fun start() {
    super.start()
    state.start()
  }

  override fun update(delta: Float) {
    super.update(delta)

    state.update(delta)
    state.nextState()?.let {
      debug(Tag.World, "$state ended -> ${it.className}")
      state.destroy()
      state = it
      state.start()
    }
  }

  override fun step(delta: Float) {
    super.step(delta)
    state.step(delta)
  }

  override fun destroy() {
    super.destroy()
    state.destroy()
  }
}